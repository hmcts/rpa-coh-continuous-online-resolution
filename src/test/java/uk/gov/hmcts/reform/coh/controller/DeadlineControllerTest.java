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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.coh.controller.state.DeadlineExtensionHelper;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.service.SessionEventService;

import java.util.Optional;
import java.util.UUID;

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

    @Test
    public void tesDeniedAndSessionEventQueued() throws Throwable {
        helper = new DeadlineExtensionHelper(1, 1, 0, 1);
        when(questionService.requestDeadlineExtension(any())).thenReturn(helper);
        OnlineHearing spyOnlineHearing = spy(OnlineHearing.class);
        Optional<OnlineHearing> onlineHearing = Optional.of(spyOnlineHearing);
        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).thenReturn(onlineHearing);

        UUID onlineHearingId = UUID.randomUUID();
        mockMvc.perform(put("/continuous-online-hearings/" + onlineHearingId + "/questions-deadline-extension"))
                .andExpect(status().is(424))
                .andExpect(content().string("Deadline extension rejected"));

        verify(sessionEventService, times(1)).createSessionEvent(onlineHearing.get(), EventTypes.QUESTION_DEADLINE_EXTENSION_DENIED.getEventType());
    }

    @Test
    public void testGrantedAndSessionEventQueued() throws Throwable {
        helper = new DeadlineExtensionHelper(1, 1, 1, 0);
        when(questionService.requestDeadlineExtension(any())).thenReturn(helper);
        OnlineHearing spyOnlineHearing = spy(OnlineHearing.class);
        Optional<OnlineHearing> onlineHearing = Optional.of(spyOnlineHearing);
        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).thenReturn(onlineHearing);

        UUID onlineHearingId = UUID.randomUUID();
        mockMvc.perform(put("/continuous-online-hearings/" + onlineHearingId + "/questions-deadline-extension"))
                .andExpect(status().isOk());
        verify(sessionEventService, times(1)).createSessionEvent(onlineHearing.get(), EventTypes.QUESTION_DEADLINE_EXTENSION_GRANTED.getEventType());
    }

    @Test
    public void testOnlySomeAreGrantedAndSessionEventQueued() throws Throwable {
        helper = new DeadlineExtensionHelper(2, 2, 1, 1);
        when(questionService.requestDeadlineExtension(any())).thenReturn(helper);
        OnlineHearing spyOnlineHearing = spy(OnlineHearing.class);
        Optional<OnlineHearing> onlineHearing = Optional.of(spyOnlineHearing);
        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).thenReturn(onlineHearing);

        UUID onlineHearingId = UUID.randomUUID();
        mockMvc.perform(put("/continuous-online-hearings/" + onlineHearingId + "/questions-deadline-extension"))
            .andExpect(status().isOk());
        verify(sessionEventService, times(1)).createSessionEvent(onlineHearing.get(), EventTypes.QUESTION_DEADLINE_EXTENSION_GRANTED.getEventType());
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
