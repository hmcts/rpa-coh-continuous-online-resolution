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
import uk.gov.hmcts.reform.coh.controller.utils.CohISO8601DateFormat;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.RelistingState;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.JurisdictionService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;
import uk.gov.hmcts.reform.coh.service.SessionEventService;
import uk.gov.hmcts.reform.coh.util.OnlineHearingEntityUtils;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder.buildOnlineHearingGet;
import static uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder.buildRelistingGet;

@RunWith(SpringRunner.class)
@ActiveProfiles({"local"})
@WebMvcTest({OnlineHearingController.class})
public class RelistingControllerTest {

    @MockBean
    private WebConfig webConfig;

    @MockBean
    private OnlineHearingService onlineHearingService;

    @MockBean
    private OnlineHearingStateService onlineHearingStateService;

    @MockBean
    private JurisdictionService jurisdictionService;

    @MockBean
    private SessionEventService sessionEventService;

    @MockBean
    private Clock clock;

    @Autowired
    private MockMvc mockMvc;
    private OnlineHearing onlineHearing;
    private String pathToExistingOnlineHearing;
    private String pathToExistingRelisting;
    private String pathToNonExistingRelisting;

    @Before
    public void setup() {
        onlineHearing = OnlineHearingEntityUtils.createTestOnlineHearingEntity();
        when(onlineHearingService.retrieveOnlineHearing(onlineHearing.getOnlineHearingId()))
            .thenReturn(Optional.of(onlineHearing));

        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class)))
            .thenReturn(Optional.of(onlineHearing));

        when(clock.instant()).thenReturn(Instant.now());

        pathToExistingOnlineHearing = buildOnlineHearingGet(onlineHearing.getOnlineHearingId());
        pathToExistingRelisting = buildRelistingGet(onlineHearing.getOnlineHearingId());
        pathToNonExistingRelisting = buildRelistingGet(UUID.randomUUID());
    }

    @Test
    public void initiallyReasonIsDraftedAndEmpty() throws Exception {
        mockMvc.perform(get(pathToExistingOnlineHearing).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.relisting.reason", isEmptyOrNullString()))
            .andExpect(jsonPath("$.relisting.state", is(RelistingState.DRAFTED.toString())))
            .andExpect(jsonPath("$.relisting.created", isEmptyOrNullString()))
            .andExpect(jsonPath("$.relisting.updated", isEmptyOrNullString()));
    }

    @Test
    public void storingADraftReturnsAccepted() throws Exception {
        String request = JsonUtils.getJsonInput("relisting/valid-drafted");
        mockMvc.perform(put(pathToExistingRelisting).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted());
    }

    @Test
    public void storesDraftedReasonWithCorrespondingOnlineHearing() throws Exception {
        String request = JsonUtils.getJsonInput("relisting/valid-drafted");
        mockMvc.perform(put(pathToExistingRelisting).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted());

        mockMvc.perform(get(pathToExistingOnlineHearing).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.relisting.reason", is("Here is a sample reason.")))
            .andExpect(jsonPath("$.relisting.state", is("DRAFTED")));
    }

    @Test
    public void creatingNewRelistForNonExistingOnlineHearingReturns404() throws Exception {
        String request = JsonUtils.getJsonInput("relisting/valid-drafted");
        mockMvc.perform(put(pathToNonExistingRelisting).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Not found"));
    }

    @Test
    public void returns400WhenCreatingWithInvalidState() throws Exception {
        String request = JsonUtils.getJsonInput("relisting/invalid-state");
        mockMvc.perform(put(pathToExistingRelisting).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void cannotUpdateReasonAfterIssuing() throws Exception {
        String req1 = JsonUtils.getJsonInput("relisting/valid-issued-1");
        mockMvc.perform(put(pathToExistingRelisting).content(req1).contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted());

        String req2 = JsonUtils.getJsonInput("relisting/valid-issued-2");
        mockMvc.perform(put(pathToExistingRelisting).content(req2).contentType(APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(content().string("Already issued"));

        mockMvc.perform(get(pathToExistingOnlineHearing).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.relisting.reason", is("Reason 1")))
            .andExpect(jsonPath("$.relisting.state", is("ISSUE_PENDING")));
    }

    @Test
    public void relistingStateIsCaseInsensitive() throws Exception {
        String request = JsonUtils.getJsonInput("relisting/valid-drafted-case-insensitive");
        mockMvc.perform(put(pathToExistingRelisting).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted());

        mockMvc.perform(get(pathToExistingOnlineHearing).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.relisting.reason", is("Test")))
            .andExpect(jsonPath("$.relisting.state", is("DRAFTED")));
    }

    @Test
    public void creatingRelistingRequiresStateField() throws Exception {
        String request = "{}";
        mockMvc.perform(put(pathToExistingRelisting).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void relistingAddsEventToSessionQueue() throws Exception {
        onlineHearing.setRelistState(RelistingState.DRAFTED);

        String request = JsonUtils.getJsonInput("relisting/valid-issued-1");
        mockMvc.perform(put(pathToExistingRelisting).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted());

        String relisted = EventTypes.ONLINE_HEARING_RELISTED.getEventType();
        verify(sessionEventService, times(1)).createSessionEvent(onlineHearing, relisted);

        assertThat(onlineHearing.getRelistState()).isEqualTo(RelistingState.ISSUE_PENDING);
    }

    @Test
    public void relistingSetsEndDate() throws Exception {
        // clearing before test
        onlineHearing.setEndDate(null);

        String request = JsonUtils.getJsonInput("relisting/valid-issued-1");
        mockMvc.perform(put(pathToExistingRelisting).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted());

        ArgumentCaptor<OnlineHearing> onlineHearingCaptor = ArgumentCaptor.forClass(OnlineHearing.class);
        verify(onlineHearingService, times(1)).updateOnlineHearing(onlineHearingCaptor.capture());

        assertThat(onlineHearingCaptor.getValue().getEndDate()).isCloseTo(new Date(), 1000);
    }

    @Test
    public void relistingDraftForTheFirstTimeSetsRelistCreatedField() throws Exception {
        String request = JsonUtils.getJsonInput("relisting/valid-drafted");
        mockMvc.perform(put(pathToExistingRelisting).content(request).contentType(APPLICATION_JSON));

        ArgumentCaptor<OnlineHearing> onlineHearingCaptor = ArgumentCaptor.forClass(OnlineHearing.class);
        verify(onlineHearingService, times(1)).updateOnlineHearing(onlineHearingCaptor.capture());

        assertThat(onlineHearingCaptor.getValue().getRelistCreated()).isNotNull();
    }

    @Test
    public void relistingDraftForTheSecondTimeDoesNotModifyCreatedField() throws Exception {
        String request = JsonUtils.getJsonInput("relisting/valid-drafted");
        mockMvc.perform(put(pathToExistingRelisting).content(request).contentType(APPLICATION_JSON));

        ArgumentCaptor<OnlineHearing> onlineHearingCaptor = ArgumentCaptor.forClass(OnlineHearing.class);
        verify(onlineHearingService, times(1)).updateOnlineHearing(onlineHearingCaptor.capture());

        Date created = onlineHearingCaptor.getValue().getRelistCreated();

        Instant after1Second = created.toInstant().plus(1, ChronoUnit.SECONDS);
        when(clock.instant()).thenReturn(after1Second);

        request = JsonUtils.getJsonInput("relisting/valid-drafted-case-insensitive");
        mockMvc.perform(put(pathToExistingRelisting).content(request).contentType(APPLICATION_JSON));

        mockMvc.perform(get(pathToExistingOnlineHearing).contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.relisting.created", is(CohISO8601DateFormat.format(created))));
    }

    @Test
    public void relistingDraftForTheSecondTimeModifiesUpdatedField() throws Exception {
        String request = JsonUtils.getJsonInput("relisting/valid-drafted");
        mockMvc.perform(put(pathToExistingRelisting).content(request).contentType(APPLICATION_JSON));

        ArgumentCaptor<OnlineHearing> onlineHearingCaptor = ArgumentCaptor.forClass(OnlineHearing.class);
        verify(onlineHearingService, times(1)).updateOnlineHearing(onlineHearingCaptor.capture());

        Date updated = onlineHearingCaptor.getValue().getRelistUpdated();

        Instant after1Second = updated.toInstant().plus(1, ChronoUnit.SECONDS);
        when(clock.instant()).thenReturn(after1Second);

        request = JsonUtils.getJsonInput("relisting/valid-drafted-case-insensitive");
        mockMvc.perform(put(pathToExistingRelisting).content(request).contentType(APPLICATION_JSON));

        Date expected = Date.from(after1Second);

        mockMvc.perform(get(pathToExistingOnlineHearing).contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.relisting.updated", is(CohISO8601DateFormat.format(expected))));
    }

    @Test
    public void relistingDraftModifiesUpdatedOnlyWhenValuesAreChanged() throws Exception {
        String request = JsonUtils.getJsonInput("relisting/valid-drafted");
        mockMvc.perform(put(pathToExistingRelisting).content(request).contentType(APPLICATION_JSON));

        ArgumentCaptor<OnlineHearing> onlineHearingCaptor = ArgumentCaptor.forClass(OnlineHearing.class);
        verify(onlineHearingService, times(1)).updateOnlineHearing(onlineHearingCaptor.capture());

        Date updated = onlineHearingCaptor.getValue().getRelistUpdated();

        Instant after1Second = updated.toInstant().plus(1, ChronoUnit.SECONDS);
        when(clock.instant()).thenReturn(after1Second);

        mockMvc.perform(put(pathToExistingRelisting).content(request).contentType(APPLICATION_JSON));

        mockMvc.perform(get(pathToExistingOnlineHearing).contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.relisting.updated", is(CohISO8601DateFormat.format(updated))));
    }

    @Test
    public void relistingWithIssuePendingReturnsBadRequest() throws Exception {
        String request = JsonUtils.getJsonInput("relisting/invalid-issue-pending");
        mockMvc.perform(put(pathToExistingRelisting).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Invalid state"));
    }

    @Test
    public void draftingAlreadyIssuedRelistReturnsConflict() throws Exception {
        onlineHearing.setRelistState(RelistingState.ISSUED);

        String request = JsonUtils.getJsonInput("relisting/valid-drafted");
        mockMvc.perform(put(pathToExistingRelisting).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isConflict());
    }
}
