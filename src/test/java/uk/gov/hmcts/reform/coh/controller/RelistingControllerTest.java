package uk.gov.hmcts.reform.coh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.coh.config.WebConfig;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.RelistingResponse;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.util.OnlineHearingEntityUtils;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles({"local"})
@WebMvcTest({RelistingController.class})
public class RelistingControllerTest {

    @MockBean
    private WebConfig webConfig;

    @MockBean
    private OnlineHearingService onlineHearingService;

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

        pathToExistingOnlineHearing = "/continuous-online-hearings/" + onlineHearing.getOnlineHearingId() + "/relist";
        pathToNonExistingOnlineHearing = "/continuous-online-hearings/" + UUID.randomUUID() + "/relist";
    }

    @Test
    public void initiallyReasonIsDraftedAndEmpty() throws Exception {
        mockMvc.perform(get(pathToExistingOnlineHearing).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reason", isEmptyOrNullString()))
            .andExpect(jsonPath("$.state", is("DRAFTED")));
    }

    @Test
    public void storingADraftReturnsAccepted() throws Exception {
        RelistingResponse obj = new RelistingResponse("Foo bar", "DRAFTED");
        String request = new ObjectMapper().writeValueAsString(obj);
        mockMvc.perform(post(pathToExistingOnlineHearing).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted());
    }

    @Test
    public void storesDraftedReasonWithCorrespondingOnlineHearing() throws Exception {
        RelistingResponse obj = new RelistingResponse("Here is a sample reason.", "DRAFTED");
        String request = new ObjectMapper().writeValueAsString(obj);
        mockMvc.perform(post(pathToExistingOnlineHearing).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isAccepted());

        mockMvc.perform(get(pathToExistingOnlineHearing).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reason", is(obj.reason)))
            .andExpect(jsonPath("$.state", is(obj.state)));
    }

    @Test
    public void readingNonExistingOnlineHearingReturns404() throws Exception {
        mockMvc.perform(get(pathToNonExistingOnlineHearing).contentType(APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Not found"));
    }

    @Test
    public void creatingNewRelistForNonExistingOnlineHearingReturns404() throws Exception {
        RelistingResponse obj = new RelistingResponse("Foo bar", "DRAFTED");
        String request = new ObjectMapper().writeValueAsString(obj);
        mockMvc.perform(post(pathToNonExistingOnlineHearing).content(request).contentType(APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Not found"));
    }
}
