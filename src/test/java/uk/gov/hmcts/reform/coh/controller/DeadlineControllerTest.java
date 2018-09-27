package uk.gov.hmcts.reform.coh.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.coh.controller.state.DeadlineExtensionHelper;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.service.SessionEventService;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local"})
public class DeadlineControllerTest {

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private QuestionService questionService;

    @Mock
    private SessionEventService sessionEventService;

    @InjectMocks
    private DeadlineController deadlineController;

    @Autowired
    private MockMvc mockMvc;

    private DeadlineExtensionHelper helper;

    @Before
    public void setUp() {
        setDeadlineExtensionHelper(1, 1, 0, 0);
        mockMvc = MockMvcBuilders.standaloneSetup(deadlineController).build();
    }

    @Test
    public void testOnlineHearingNotFound() throws Exception {
        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).thenReturn(Optional.empty());

        UUID onlineHearingId = UUID.randomUUID();
        mockMvc.perform(put("/continuous-online-hearings/" + onlineHearingId + "/questions-deadline-extension"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testNoTotal() throws Exception {
        OnlineHearing spyOnlineHearing = spy(OnlineHearing.class);
        Optional<OnlineHearing> onlineHearing = Optional.of(spyOnlineHearing);
        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).thenReturn(onlineHearing);
        setDeadlineExtensionHelper(0, 0, 0, 0);

        UUID onlineHearingId = UUID.randomUUID();
        mockMvc.perform(put("/continuous-online-hearings/" + onlineHearingId + "/questions-deadline-extension"))
                .andExpect(status().is(424))
        .andReturn().getResponse().getContentAsString().equals("No questions to extend deadline for");
    }

    @Test
    public void testNoEligible() throws Exception {
        OnlineHearing spyOnlineHearing = spy(OnlineHearing.class);
        Optional<OnlineHearing> onlineHearing = Optional.of(spyOnlineHearing);
        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).thenReturn(onlineHearing);
        setDeadlineExtensionHelper(1, 0, 0, 0);

        UUID onlineHearingId = UUID.randomUUID();
        mockMvc.perform(put("/continuous-online-hearings/" + onlineHearingId + "/questions-deadline-extension"))
                .andExpect(status().is(424))
                .andReturn().getResponse().getContentAsString().equals("No questions to extend deadline for");
    }

    @Test
    public void testNoGrantedNoDenied() throws Exception {
        helper = new DeadlineExtensionHelper(1, 1, 0, 0);
        OnlineHearing spyOnlineHearing = spy(OnlineHearing.class);
        Optional<OnlineHearing> onlineHearing = Optional.of(spyOnlineHearing);
        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).thenReturn(onlineHearing);

        UUID onlineHearingId = UUID.randomUUID();
        mockMvc.perform(put("/continuous-online-hearings/" + onlineHearingId + "/questions-deadline-extension"))
                .andExpect(status().isOk());
    }

    /**
     * This is to let {@link Consumer#accept(Object)} to throw {@link Exception}s.
     */
    interface TestConsumer {
        void accept(ResultActions actions) throws Exception;
    }

    /**
     * Almost Swiss-army knife method for asserting "what happens when" requesting deadline extension.
     * @param total number of all questions in the hearing
     * @param eligible number of questions that passed the basic filtering
     * @param granted number of questions that were granted extension
     * @param denied number of questions that were denied extension
     * @param expectedStatus type of session event expected to be triggered after the request
     * @param consumer lambda function to execute additional assertions on the response
     * @throws Exception when assertion fails, or anything goes awry
     */
    private void requestedExtension(
        int total,
        int eligible,
        int granted,
        int denied,
        EventTypes expectedStatus,
        TestConsumer consumer
    ) throws Exception {
        helper = new DeadlineExtensionHelper(total, eligible, granted, denied);
        when(questionService.requestDeadlineExtension(any())).thenReturn(helper);
        OnlineHearing spyOnlineHearing = spy(OnlineHearing.class);
        Optional<OnlineHearing> onlineHearing = Optional.of(spyOnlineHearing);
        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).thenReturn(onlineHearing);

        UUID onlineHearingId = UUID.randomUUID();
        String path = "/continuous-online-hearings/" + onlineHearingId + "/questions-deadline-extension";

        consumer.accept(mockMvc.perform(put(path)));

        verify(sessionEventService, times(1)).createSessionEvent(onlineHearing.get(), expectedStatus.getEventType());
    }

    @Test
    public void tesDeniedAndSessionEventQueued() throws Throwable {
        requestedExtension(1, 1, 0, 1, EventTypes.QUESTION_DEADLINE_EXTENSION_DENIED, resultActions
            -> resultActions
                .andExpect(status().is(424))
                .andExpect(content().string("Deadline extension rejected"))
        );
    }

    @Test
    public void testGrantedAndSessionEventQueued() throws Throwable {
        requestedExtension(1, 1, 1, 0, EventTypes.QUESTION_DEADLINE_EXTENSION_GRANTED, resultActions
            -> resultActions
                .andExpect(status().isOk())
        );
    }

    @Test
    public void testOnlySomeAreGrantedAndSessionEventQueued() throws Throwable {
        requestedExtension(2, 2, 1, 1, EventTypes.QUESTION_DEADLINE_EXTENSION_GRANTED, resultActions
            -> resultActions
                .andExpect(status().isOk())
        );
    }

    @Test
    public void testSuccessfulExtensionRequest() throws Exception {
        OnlineHearing spyOnlineHearing = spy(OnlineHearing.class);
        Optional<OnlineHearing> onlineHearing = Optional.of(spyOnlineHearing);
        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).thenReturn(onlineHearing);

        UUID onlineHearingId = UUID.randomUUID();
        mockMvc.perform(put("/continuous-online-hearings/" + onlineHearingId + "/questions-deadline-extension"))
            .andExpect(status().isOk());
    }

    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    @Test
    public void testExtensionRequestWithExceptionThrown() throws Exception {
        OnlineHearing spyOnlineHearing = spy(OnlineHearing.class);
        Optional<OnlineHearing> onlineHearing = Optional.of(spyOnlineHearing);
        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).thenReturn(onlineHearing);

        doThrow(RuntimeException.class).when(questionService).requestDeadlineExtension(any());

        UUID onlineHearingId = UUID.randomUUID();
        mockMvc.perform(put("/continuous-online-hearings/" + onlineHearingId + "/questions-deadline-extension"))
            .andExpect(status().is5xxServerError());
    }

    private void setDeadlineExtensionHelper(long total, long eligible, long granted, long denied) {
        helper = new DeadlineExtensionHelper(total, eligible, granted, denied);
        when(questionService.requestDeadlineExtension(any())).thenReturn(helper);
    }
}
