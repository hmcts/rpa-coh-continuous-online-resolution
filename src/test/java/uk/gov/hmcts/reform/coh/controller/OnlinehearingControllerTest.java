package uk.gov.hmcts.reform.coh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.coh.controller.onlinehearing.CreateOnlinehearingResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlinehearingRequest;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.Onlinehearing;
import uk.gov.hmcts.reform.coh.domain.OnlinehearingPanelMember;
import uk.gov.hmcts.reform.coh.domain.Onlinehearingstate;
import uk.gov.hmcts.reform.coh.service.JurisdictionService;
import uk.gov.hmcts.reform.coh.service.OnlinehearingPanelMemberService;
import uk.gov.hmcts.reform.coh.service.OnlinehearingService;
import uk.gov.hmcts.reform.coh.service.OnlinehearingStateService;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local"})
public class OnlinehearingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private OnlinehearingService onlinehearingService;

    @Mock
    private OnlinehearingPanelMemberService onlinehearingPanelMemberService;

    @Mock
    private OnlinehearingStateService onlinehearingStateService;

    @Mock
    private JurisdictionService jurisdictionService;

    private static final String ENDPOINT = "/continuous-online-hearings";

    @InjectMocks
    private OnlinehearingController onlinehearingController;

    private UUID uuid;

    private Onlinehearing onlinehearing;

    private Onlinehearingstate onlinehearingstate;

    private OnlinehearingPanelMember member;

    @Before
    public void setup(){
        uuid = UUID.randomUUID();
        onlinehearing = new Onlinehearing();
        onlinehearing.setOnlinehearingId(uuid);
        member = new OnlinehearingPanelMember();
        member.setFullName("foo bar");
        onlinehearing.setPanelMembers(Arrays.asList(member));
        mockMvc = MockMvcBuilders.standaloneSetup(onlinehearingController).build();

        onlinehearingstate = new Onlinehearingstate();
        onlinehearingstate.setOnlinehearingStateId(1);
        onlinehearingstate.setState("continuous_online_hearing_started");
        given(onlinehearingService.createOnlinehearing(any(Onlinehearing.class))).willReturn(onlinehearing);
        given(onlinehearingService.retrieveOnlinehearing(any(Onlinehearing.class))).willReturn(Optional.of(onlinehearing));
        given(jurisdictionService.getJurisdictionWithName(anyString())).willReturn(java.util.Optional.of(new Jurisdiction()));
        given(onlinehearingPanelMemberService.createOnlinehearing(any(OnlinehearingPanelMember.class))).willReturn(new OnlinehearingPanelMember());
        given(onlinehearingStateService.retrieveOnlinehearingStateByState("continuous_online_hearing_started")).willReturn(Optional.ofNullable(onlinehearingstate));
    }

    @Test
    public void testCreateOnlinehearingWithJsonFile() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("json/online_hearing/standard_online_hearing.json")).getFile());

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(mapper.readValue(file, Object.class));

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testReadOnlinehearingWithJsonFile() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testCreateOnlinehearing() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("json/online_hearing/standard_online_hearing.json")).getFile());

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(mapper.readValue(file, Object.class));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        CreateOnlinehearingResponse response = (CreateOnlinehearingResponse) JsonUtils.toObjectFromJson(result.getResponse().getContentAsString(), CreateOnlinehearingResponse.class);
        assertEquals(uuid.toString(), response.getOnlinehearingId());
    }

    @Test
    public void testCreateOnlinehearingIncorrectJurisdiction() throws Exception {

        given(jurisdictionService.getJurisdictionWithName("foo")).willReturn(java.util.Optional.empty());
        OnlinehearingRequest request = (OnlinehearingRequest) JsonUtils.toObjectFromTestName("online_hearing/standard_online_hearing", OnlinehearingRequest.class);
        request.setJurisdiction("foo");

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testCreateOnlinehearingStartingStateNotFound() throws Exception {

        given(onlinehearingStateService.retrieveOnlinehearingStateByState("continuous_online_hearing_started")).willReturn(Optional.empty());
        OnlinehearingRequest request = (OnlinehearingRequest) JsonUtils.toObjectFromTestName("online_hearing/standard_online_hearing", OnlinehearingRequest.class);

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(request)))
                .andExpect(status().isUnprocessableEntity());
    }
}
