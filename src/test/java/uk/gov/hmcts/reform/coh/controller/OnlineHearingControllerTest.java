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
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.service.JurisdictionService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;

import java.io.File;
import java.util.Objects;

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
    private JurisdictionService jurisdictionService;

    private static final String ENDPOINT = "/online-hearings";

    @InjectMocks
    private OnlineHearingController onlineHearingController;


    @Before
    public void setup(){
        mockMvc = MockMvcBuilders.standaloneSetup(onlineHearingController).build();
        given(onlineHearingService.createOnlineHearing(any(OnlineHearing.class))).willReturn(new OnlineHearing());
        given(jurisdictionService.getJurisdictionWithName(anyString())).willReturn(java.util.Optional.of(new Jurisdiction()));
    }

    @Test
    public void testCreateOnlineHearingWithJsonFile() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("json/create_online_hearing.json")).getFile());

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(mapper.readValue(file, Object.class));

        System.out.println("JSONSTRING" + jsonString);

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
}
