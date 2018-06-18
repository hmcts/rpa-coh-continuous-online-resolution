package uk.gov.hmcts.reform.coh.controller;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class QuestionControllerTest {

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp () throws Exception {
        String json = "{\n" +
                "\t\"onlineHearingId\": 4,\n" +
                "\t\"externalRef\" : \"case_id_123\" \n" +
                "}";

        mvc.perform(MockMvcRequestBuilders.post("/online-hearings/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void testAddQuestion() throws Exception {
        String json = "{\n" +
                "\t\"oh_id\": 4,\n" +
                "\t\"body\" : \"Test question text\" \n" +
                "}";

        mvc.perform(MockMvcRequestBuilders.post("/online-hearings/4/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetQuestion() throws Exception {
        String json = "{\n" +
                "\t\"oh_id\": 4,\n" +
                "}";

        mvc.perform(MockMvcRequestBuilders.post("/online-hearings/4/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void testEditQuestion() throws Exception {
        String json = "{\n" +
                "\t\"oh_id\": 4,\n" +
                "\t\"body\" : \"Test edit question text\" \n" +
                "}";

        mvc.perform(MockMvcRequestBuilders.post("/online-hearings/4/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

}
