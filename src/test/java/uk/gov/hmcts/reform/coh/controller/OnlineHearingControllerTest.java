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
import uk.gov.hmcts.reform.coh.controller.onlinehearing.CreateOnlineHearingResponse;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingPanelMember;
import uk.gov.hmcts.reform.coh.service.JurisdictionService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingPanelMemberService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.io.File;
import java.util.Objects;
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
public class OnlineHearingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private OnlineHearingPanelMemberService onlineHearingPanelMemberService;

    @Mock
    private JurisdictionService jurisdictionService;

    private static final String ENDPOINT = "/online-hearings";

    @InjectMocks
    private OnlineHearingController onlineHearingController;

    private UUID uuid;

    private OnlineHearing onlineHearing;

    @Before
    public void setup(){
        uuid = UUID.randomUUID();
        onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(uuid);
        mockMvc = MockMvcBuilders.standaloneSetup(onlineHearingController).build();
        given(onlineHearingService.createOnlineHearing(any(OnlineHearing.class))).willReturn(onlineHearing);
        given(jurisdictionService.getJurisdictionWithName(anyString())).willReturn(java.util.Optional.of(new Jurisdiction()));
        given(onlineHearingPanelMemberService.createOnlineHearing(any(OnlineHearingPanelMember.class))).willReturn(new OnlineHearingPanelMember());
    }

    @Test
    public void testCreateOnlineHearingWithJsonFile() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("json/create_online_hearing.json")).getFile());

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(mapper.readValue(file, Object.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/online-hearings/")
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testReadOnlineHearingWithJsonFile() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/case_id_123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testCreateOnlineHearing() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("json/online_hearing/standard_online_hearing.json")).getFile());

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(mapper.readValue(file, Object.class));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/online-hearings/new")
                .contentType(MediaType.APPLICATION_JSON).content(jsonString))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        CreateOnlineHearingResponse response = (CreateOnlineHearingResponse) JsonUtils.toObjectFromJson(result.getResponse().getContentAsString(), CreateOnlineHearingResponse.class);
        assertEquals(uuid.toString(), response.getOnlineHearingId());
    }
}
