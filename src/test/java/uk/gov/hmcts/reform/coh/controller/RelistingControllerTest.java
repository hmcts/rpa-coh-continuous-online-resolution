package uk.gov.hmcts.reform.coh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.coh.config.WebConfig;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.RelistingResponse;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.util.OnlineHearingEntityUtils;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles({"local"})
@WebMvcTest({RelistingController.class})
public class RelistingControllerTest {

    @MockBean
    private WebConfig webConfig;

    @Autowired
    private MockMvc mockMvc;
    private OnlineHearing onlineHearing;

    @Before
    public void setup() {
        onlineHearing = OnlineHearingEntityUtils.createTestOnlineHearingEntity();
    }

    @Test
    public void initiallyReasonIsDraftedAndEmpty() throws Exception {
        String path = "/continuous-online-hearings/" + onlineHearing.getOnlineHearingId() + "/relist";
        mockMvc.perform(get(path).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reason", Matchers.isEmptyString()))
            .andExpect(jsonPath("$.state", Matchers.is("DRAFTED")));
    }

    @Test
    public void storingADraftReturnsAccepted() throws Exception {
        String path = "/continuous-online-hearings/" + onlineHearing.getOnlineHearingId() + "/relist";
        RelistingResponse obj = new RelistingResponse("Foo bar", "DRAFTED");
        String request = new ObjectMapper().writeValueAsString(obj);
        mockMvc.perform(post(path).content(request).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isAccepted());
    }
}
