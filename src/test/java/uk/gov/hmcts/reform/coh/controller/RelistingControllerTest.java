package uk.gov.hmcts.reform.coh.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.coh.config.WebConfig;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.domain.RelistingState;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;
import uk.gov.hmcts.reform.coh.states.OnlineHearingStates;
import uk.gov.hmcts.reform.coh.util.OnlineHearingEntityUtils;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder.buildRelistingGet;

@RunWith(SpringRunner.class)
@ActiveProfiles({"local"})
@WebMvcTest({RelistingController.class})
public class RelistingControllerTest {

    @MockBean
    private WebConfig webConfig;

    @MockBean
    private OnlineHearingService onlineHearingService;

    @MockBean
    private OnlineHearingStateService onlineHearingStateService;

    @Autowired
    private MockMvc mockMvc;
    private OnlineHearing onlineHearing;
    private String pathToExistingOnlineHearing;
    private String pathToNonExistingOnlineHearing;

    @Before
    public void setup() {
        onlineHearing = OnlineHearingEntityUtils.createTestOnlineHearingEntity();
        when(onlineHearingService.retrieveOnlineHearing(onlineHearing.getOnlineHearingId()))
            .thenReturn(Optional.of(onlineHearing));

        pathToExistingOnlineHearing = buildRelistingGet(onlineHearing.getOnlineHearingId());
        pathToNonExistingOnlineHearing = buildRelistingGet(UUID.randomUUID());
    }

    @Test
    public void initiallyReasonIsDraftedAndEmpty() throws Exception {
        mockMvc.perform(get(pathToExistingOnlineHearing).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reason", isEmptyOrNullString()))
            .andExpect(jsonPath("$.state", is(RelistingState.DRAFTED.toString())));
    }

    @Test
    public void storingADraftReturnsAccepted() throws Exception {
        String request = JsonUtils.getJsonInput("relisting/valid-drafted");
        mockMvc.perform(post(pathToExistingOnlineHearing).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted());
    }

    @Test
    public void storesDraftedReasonWithCorrespondingOnlineHearing() throws Exception {
        String request = JsonUtils.getJsonInput("relisting/valid-drafted");
        mockMvc.perform(post(pathToExistingOnlineHearing).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted());

        mockMvc.perform(get(pathToExistingOnlineHearing).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reason", is("Here is a sample reason.")))
            .andExpect(jsonPath("$.state", is("DRAFTED")));
    }

    @Test
    public void readingNonExistingOnlineHearingReturns404() throws Exception {
        mockMvc.perform(get(pathToNonExistingOnlineHearing).contentType(APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Not found"));
    }

    @Test
    public void creatingNewRelistForNonExistingOnlineHearingReturns404() throws Exception {
        String request = JsonUtils.getJsonInput("relisting/valid-drafted");
        mockMvc.perform(post(pathToNonExistingOnlineHearing).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Not found"));
    }

    @Test
    public void returns400WhenCreatingWithInvalidState() throws Exception {
        String request = JsonUtils.getJsonInput("relisting/invalid-state");
        mockMvc.perform(post(pathToExistingOnlineHearing).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void cannotUpdateReasonAfterIssuing() throws Exception {
        String req1 = JsonUtils.getJsonInput("relisting/valid-issued-1");
        mockMvc.perform(post(pathToExistingOnlineHearing).content(req1).contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted());

        String req2 = JsonUtils.getJsonInput("relisting/valid-issued-2");
        mockMvc.perform(post(pathToExistingOnlineHearing).content(req2).contentType(APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(content().string("Already issued"));

        mockMvc.perform(get(pathToExistingOnlineHearing).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reason", is("Reason 1")))
            .andExpect(jsonPath("$.state", is("ISSUED")));
    }

    @Test
    public void relistingStateIsCaseInsensitive() throws Exception {
        String request = JsonUtils.getJsonInput("relisting/valid-drafted-case-insensitive");
        mockMvc.perform(post(pathToExistingOnlineHearing).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted());

        mockMvc.perform(get(pathToExistingOnlineHearing).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reason", is("Test")))
            .andExpect(jsonPath("$.state", is("DRAFTED")));
    }

    @Test
    public void creatingRelistingRequiresStateField() throws Exception {
        String request = "{}";
        mockMvc.perform(post(pathToExistingOnlineHearing).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void issuedRelistingShouldChangeOnlineHearingStateToRelisted() throws Exception {
        OnlineHearingState issued = spy(OnlineHearingState.class);
        when(issued.getState()).thenReturn(OnlineHearingStates.RELISTED.getStateName());

        when(onlineHearingStateService.retrieveOnlineHearingStateByState(OnlineHearingStates.RELISTED.getStateName()))
            .thenReturn(Optional.of(issued));

        String request = JsonUtils.getJsonInput("relisting/valid-issued-1");
        mockMvc.perform(post(pathToExistingOnlineHearing).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted());

        verify(onlineHearingStateService, atLeastOnce())
            .retrieveOnlineHearingStateByState(OnlineHearingStates.RELISTED.getStateName());

        ArgumentCaptor<OnlineHearing> onlineHearingCaptor = ArgumentCaptor.forClass(OnlineHearing.class);
        verify(onlineHearingService, times(1)).updateOnlineHearing(onlineHearingCaptor.capture());

        OnlineHearing onlineHearing = onlineHearingCaptor.getValue();
        assertThat(onlineHearing.getOnlineHearingState().getState())
            .isEqualTo(OnlineHearingStates.RELISTED.getStateName());
    }
}
