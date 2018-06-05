package uk.gov.hmcts.reform.coh.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OnlineHearingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void testRetrieve() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/online-hearings/retrieve").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Greetings from Online Hearing Controller")));
    }

    @Test
    public void testCreateOnlineHearing() throws Exception {
        String json = "{\n" +
                "\t\"onlineHearingId\": 4,\n" +
                "\t\"externalRef\" : \"case_id_123\" \n" +
                "}";

        mvc.perform(MockMvcRequestBuilders.post("/online-hearings/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }
}
