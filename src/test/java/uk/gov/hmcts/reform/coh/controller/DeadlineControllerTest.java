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
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.service.exceptions.NoQuestionsAsked;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private DeadlineController deadlineController;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(deadlineController).build();
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
    public void testExtensionRequestWithExceptionThrown() throws Exception, NoQuestionsAsked {
        OnlineHearing spyOnlineHearing = spy(OnlineHearing.class);
        Optional<OnlineHearing> onlineHearing = Optional.of(spyOnlineHearing);
        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).thenReturn(onlineHearing);

        doThrow(RuntimeException.class).when(questionService).requestDeadlineExtension(any());

        UUID onlineHearingId = UUID.randomUUID();
        mockMvc.perform(put("/continuous-online-hearings/" + onlineHearingId + "/questions-deadline-extension"))
            .andExpect(status().is5xxServerError());
    }
}
