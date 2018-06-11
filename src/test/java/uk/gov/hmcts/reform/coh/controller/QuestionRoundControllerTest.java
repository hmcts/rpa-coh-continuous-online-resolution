package uk.gov.hmcts.reform.coh.controller;

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
public class QuestionRoundControllerTest extends BaseControllerTest{

    @Autowired
    private MockMvc mvc;

    @Test
    public void testCreateQuestionRound() throws Exception {

        String json = getJsonInput("question_round/create_question_round");

        mvc.perform(MockMvcRequestBuilders.post("/online-hearings/foo/question-rounds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetQuestionRound() throws Exception {

        String json = getJsonInput("question_round/create_question_round");

        mvc.perform(MockMvcRequestBuilders.get("/online-hearings/foo/question-rounds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

}
