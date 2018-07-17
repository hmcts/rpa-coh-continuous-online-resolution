package uk.gov.hmcts.reform.coh.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.*;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingPanelMember;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.service.JurisdictionService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingPanelMemberService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private OnlineHearingPanelMemberService onlineHearingPanelMemberService;

    @Mock
    private OnlineHearingStateService onlineHearingStateService;

    @Mock
    private JurisdictionService jurisdictionService;

    private static final String ENDPOINT = "/continuous-online-hearings";

    @InjectMocks
    private OnlineHearingController onlineHearingController;

    private UUID uuid;

    private OnlineHearing onlineHearing;

    private OnlineHearingState onlineHearingState;

    private OnlineHearingPanelMember member;

    private OnlineHearingRequest onlineHearingRequest;

    private UpdateOnlineHearingRequest updateOnlineHearingRequest;

    @Before
    public void setup() throws IOException {
        uuid = UUID.randomUUID();
        onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(uuid);
        member = new OnlineHearingPanelMember();
        member.setFullName("foo bar");
        onlineHearing.setPanelMembers(Arrays.asList(member));
        mockMvc = MockMvcBuilders.standaloneSetup(onlineHearingController).build();

        onlineHearingRequest = (OnlineHearingRequest) JsonUtils.toObjectFromTestName("online_hearing/standard_online_hearing", OnlineHearingRequest.class);
        updateOnlineHearingRequest = (UpdateOnlineHearingRequest) JsonUtils.toObjectFromTestName("online_hearing/update_online_hearing", UpdateOnlineHearingRequest.class);

        onlineHearingState = new OnlineHearingState();
        onlineHearingState.setState("continuous_online_hearing_started");
        onlineHearing.addState(onlineHearingState);
        given(onlineHearingService.createOnlineHearing(any(OnlineHearing.class))).willReturn(onlineHearing);
        given(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.of(onlineHearing));
        given(jurisdictionService.getJurisdictionWithName(anyString())).willReturn(java.util.Optional.of(new Jurisdiction()));
        given(onlineHearingPanelMemberService.createOnlineHearing(any(OnlineHearingPanelMember.class))).willReturn(new OnlineHearingPanelMember());
        given(onlineHearingStateService.retrieveOnlineHearingStateByState("continuous_online_hearing_started")).willReturn(Optional.ofNullable(onlineHearingState));

    }

    @Test
    public void testCreateOnlineHearingWithJsonFile() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(onlineHearingRequest)))
                .andExpect(status().is2xxSuccessful());
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
    public void testCreateOnlineHearing() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(onlineHearingRequest)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        CreateOnlineHearingResponse response = (CreateOnlineHearingResponse) JsonUtils.toObjectFromJson(result.getResponse().getContentAsString(), CreateOnlineHearingResponse.class);
        try {
            UUID.fromString(response.getOnlineHearingId());
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
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
    public void testCreateOnlineHearingPanelNotPresent() throws Exception {

        onlineHearingRequest.setPanel(null);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(onlineHearingRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();
        assertEquals("Panel is required", result.getResponse().getContentAsString());
    }

    @Test
    public void testCreateOnlineHearingPanelMemberNameNotPresent() throws Exception {

        OnlineHearingRequest request = (OnlineHearingRequest) JsonUtils.toObjectFromTestName("online_hearing/standard_online_hearing", OnlineHearingRequest.class);
        request.getPanel().get(0).setName(null);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(request)))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();
        assertEquals("The panel member identity and name are required", result.getResponse().getContentAsString());
    }

    @Test
    public void testCreateOnlineHearingPanelMemberIdentityTokenNotPresent() throws Exception {

        onlineHearingRequest.getPanel().get(0).setIdentityToken(null);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(onlineHearingRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();
        assertEquals("The panel member identity and name are required", result.getResponse().getContentAsString());
    }

    @Test
    public void testCreateOnlineHearingStartingStateNotFound() throws Exception {

        given(onlineHearingStateService.retrieveOnlineHearingStateByState("continuous_online_hearing_started")).willReturn(Optional.empty());

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

        OnlineHearingsResponse onlineHearingsResponse = (OnlineHearingsResponse)JsonUtils.toObjectFromJson(result.getResponse().getContentAsString(), OnlineHearingsResponse.class);
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
    public void testUpdateNonExistentOnlineHearing() throws Exception {
        given(onlineHearingService.retrieveOnlineHearing(uuid)).willReturn(Optional.empty());
        updateOnlineHearingRequest.setState(OnlineHearingStates.QUESTIONS_ISSUED.getStateName());

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(updateOnlineHearingRequest)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Online hearing not found");
    }

    @Test
    public void testUpdateOnlineHearingWithInvalidState() throws Exception {
        given(onlineHearingService.retrieveOnlineHearing(uuid)).willReturn(Optional.of(onlineHearing));
        updateOnlineHearingRequest.setState("foo");

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(updateOnlineHearingRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Invalid state");
    }

    @Test
    public void testUpdateOnlineHearing() throws Exception {
        given(onlineHearingService.retrieveOnlineHearing(uuid)).willReturn(Optional.of(onlineHearing));
        onlineHearingState.setState(OnlineHearingStates.QUESTIONS_ISSUED.getStateName());
        given(onlineHearingStateService.retrieveOnlineHearingStateByState("continuous_online_hearing_questions_issued")).willReturn(Optional.of(onlineHearingState));
        updateOnlineHearingRequest.setState(OnlineHearingStates.QUESTIONS_ISSUED.getStateName());

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(updateOnlineHearingRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Online hearing updated");
    }

    @Test
    public void testUpdateOnlineHearingWithStartedStateFails() throws Exception {
        given(onlineHearingService.retrieveOnlineHearing(uuid)).willReturn(Optional.of(onlineHearing));
        given(onlineHearingStateService.retrieveOnlineHearingStateByState("continuous_online_hearing_started")).willReturn(Optional.of(onlineHearingState));
        updateOnlineHearingRequest.setState(OnlineHearingStates.STARTED.getStateName());

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(updateOnlineHearingRequest)))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Online hearing state cannot be changed back to started");

    }

}
