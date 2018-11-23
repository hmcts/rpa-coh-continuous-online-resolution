package uk.gov.hmcts.reform.coh.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.*;
import uk.gov.hmcts.reform.coh.controller.utils.CohISO8601DateFormat;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.*;
import uk.gov.hmcts.reform.coh.util.SessionEventUtils;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.coh.states.OnlineHearingStates.RELISTED;
import static uk.gov.hmcts.reform.coh.states.OnlineHearingStates.STARTED;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local"})
public class OnlineHearingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private OnlineHearingStateService onlineHearingStateService;

    @Mock
    private JurisdictionService jurisdictionService;

    @Mock
    private SessionEventTypeService sessionEventTypeService;

    @Mock
    private SessionEventService sessionEventService;

    private SessionEventType eventType;

    private static final String ENDPOINT = "/continuous-online-hearings";

    @InjectMocks
    private OnlineHearingController onlineHearingController;

    private UUID uuid;

    private OnlineHearing onlineHearing;

    private OnlineHearingState onlineHearingState;

    private OnlineHearingRequest onlineHearingRequest;

    @Before
    public void setup() throws IOException {
        uuid = UUID.randomUUID();
        onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(uuid);
        onlineHearing.setStartDate(new Date());

        mockMvc = MockMvcBuilders.standaloneSetup(onlineHearingController).build();

        onlineHearingRequest = JsonUtils.toObjectFromTestName("online_hearing/standard_online_hearing", OnlineHearingRequest.class);
        onlineHearingState = new OnlineHearingState();
        onlineHearingState.setState("continuous_online_hearing_started");
        onlineHearing.setOnlineHearingState(onlineHearingState);
        onlineHearing.addOnlineHearingStateHistory(onlineHearingState);
        eventType = SessionEventUtils.get(EventTypes.ONLINE_HEARING_RELISTED);

        given(onlineHearingService.createOnlineHearing(any(OnlineHearing.class))).willReturn(onlineHearing);
        given(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.of(onlineHearing));
        given(jurisdictionService.getJurisdictionWithName(anyString())).willReturn(java.util.Optional.of(new Jurisdiction()));
        given(onlineHearingStateService.retrieveOnlineHearingStateByState(STARTED.getStateName())).willReturn(Optional.ofNullable(onlineHearingState));
        given(onlineHearingStateService.retrieveOnlineHearingStateByState(RELISTED.getStateName())).willReturn(Optional.ofNullable(onlineHearingState));
        given(sessionEventTypeService.retrieveEventType(EventTypes.ONLINE_HEARING_RELISTED.getEventType())).willReturn(Optional.of(eventType));
        given(sessionEventService.createSessionEvent(any(OnlineHearing.class), any(SessionEventType.class))).willReturn(new SessionEvent());
    }

    @Test
    public void testCreateOnlineHearingWithJsonFileAndCheckLocationHeader() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(onlineHearingRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String returnedUrl = result.getResponse().getHeader("Location");
        try {
            URL u = new URL(returnedUrl); // this would check for the protocol
            u.toURI(); // does the extra checking required for validation of URI
            assertTrue(true);
        }catch(MalformedURLException e){
            fail();
        }
    }

    @Test
    public void testReadOnlineHearingWithJsonFile() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testOnlineHearingNotExists() throws Exception {
        given(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void testCreateOnlineHearingAndCheckLocationHeader() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(onlineHearingRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        CreateOnlineHearingResponse response = JsonUtils.toObjectFromJson(result.getResponse().getContentAsString(), CreateOnlineHearingResponse.class);
        try {
            UUID.fromString(response.getOnlineHearingId());
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
        }

        String returnedUrl = result.getResponse().getHeader("Location");
        try {
            URL u = new URL(returnedUrl); // this would check for the protocol
            u.toURI(); // does the extra checking required for validation of URI
            assertTrue(true);
        } catch(MalformedURLException e){
            fail();
        }
    }

    @Test
    public void testCreateOnlineHearingDuplicate() throws Exception {

        given(onlineHearingService.retrieveOnlineHearingByCaseIds(Arrays.asList("case_123"))).willReturn(Arrays.asList(onlineHearing));
        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(onlineHearingRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testCreateOnlineHearingIncorrectJurisdiction() throws Exception {

        given(jurisdictionService.getJurisdictionWithName("foo")).willReturn(java.util.Optional.empty());
        onlineHearingRequest.setJurisdiction("foo");

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(onlineHearingRequest)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testCreateOnlineHearingCaseIdNotPresent() throws Exception {

        onlineHearingRequest.setCaseId(null);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(onlineHearingRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();
        assertEquals("Case id is required", result.getResponse().getContentAsString());
    }

    @Test
    public void testCreateOnlineHearingStartingStateAndJurisdictionNotFound() throws Exception {

        given(onlineHearingStateService.retrieveOnlineHearingStateByState("continuous_online_hearing_started")).willReturn(Optional.empty());
        given(jurisdictionService.getJurisdictionWithName(anyString())).willReturn(java.util.Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(onlineHearingRequest)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testFilterOnlineHearingByCaseId() throws Exception {

        given(onlineHearingService.retrieveOnlineHearingByCaseIds(Arrays.asList("case1"), Optional.empty())).willReturn(Arrays.asList(onlineHearing));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "?case_id=case1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        OnlineHearingsResponse onlineHearingsResponse = JsonUtils.toObjectFromJson(result.getResponse().getContentAsString(), OnlineHearingsResponse.class);
        assertEquals(1, onlineHearingsResponse.getOnlineHearingResponses().size());
    }

    @Test
    public void testFilterOnlineHearingByCaseIdEmpty() throws Exception {

        given(onlineHearingService.retrieveOnlineHearingByCaseIds(Arrays.asList("case1"))).willReturn(Arrays.asList());

        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "?case_id=case1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk());
    }

    @Test
    public void testFilterOnlineHearingByCaseIdAndState() throws Exception {

        given(onlineHearingService.retrieveOnlineHearingByCaseIds(Arrays.asList("case1"))).willReturn(Arrays.asList());

        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "?case_id=case1&state=foo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk());
    }

    @Test
    public void testOnlineHearingHistoryIsSorted() throws Exception {
        onlineHearingState.setOnlineHearingStateId(2);
        onlineHearingState.setState("continuous_online_hearing_questions_issued");
        onlineHearing.setOnlineHearingState(onlineHearingState);
        onlineHearing.addOnlineHearingStateHistory(onlineHearingState);
        Date recentStateTime = onlineHearing.getOnlineHearingStateHistories().get(onlineHearing.getOnlineHearingStateHistories().size() - 1).getDateOccurred();
        onlineHearing.getOnlineHearingStateHistories().get(0).setDateOccurred(new Date(2017, 12, 31));

        OnlineHearingResponse response = new OnlineHearingResponse();
        OnlineHearingMapper.map(response, onlineHearing);

        assertEquals(CohISO8601DateFormat.format(recentStateTime), response.getCurrentState().getDatetime());
    }

    @Test
    public void testUseIdAMIDAsUserReferenceWhenCreatingEntity() throws Exception {

        // this is required to prevent MockMvc instantiating clean Security Context
        mockMvc = MockMvcBuilders.standaloneSetup(onlineHearingController)
            .addFilter(new SecurityContextPersistenceFilter())
            .build();

        String username = "1234567";
        String token = "nothing interesting";
        Collection<String> authorities = Collections.emptyList();
        String serviceName = "acme";
        ServiceAndUserDetails principal = new ServiceAndUserDetails(username, token, authorities, serviceName);

        Authentication authentication = spy(Authentication.class);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getPrincipal()).willReturn(principal);

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
            .with(authentication(authentication))
            .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(onlineHearingRequest)))
            .andExpect(status().isCreated());

        ArgumentCaptor<OnlineHearing> onlineHearingArgumentCaptor = ArgumentCaptor.forClass(OnlineHearing.class);

        verify(onlineHearingService, times(1)).createOnlineHearing(onlineHearingArgumentCaptor.capture());

        assertEquals(username, onlineHearingArgumentCaptor.getValue().getOwnerReferenceId());
    }
}
