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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.coh.bdd.steps.JsonUtils;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerRequest;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.QuestionService;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AnswerControllerTest {

    @Mock
    private QuestionService questionService;

    @Mock
    private AnswerService answerService;

    @InjectMocks
    private AnswerController answerController;

    @Autowired
    private MockMvc mockMvc;

    private static final String ENDPOINT = "/online-hearings/1/questions/1/answers";

    private AnswerRequest request;

    @Before
    public void setup() throws IOException {
        mockMvc = MockMvcBuilders.standaloneSetup(answerController).build();
        given(questionService.retrieveQuestionById(any(Long.class))).willReturn(new Question());
        given(answerService.createAnswer(any(Answer.class))).willReturn(new Answer());
        request = (AnswerRequest) JsonUtils.toObject("answer/standard_answer", AnswerRequest.class);
    }

    @Test
    public void testEmptyAnswerText() throws Exception {

        request.getAnswer().setAnswer(null);

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testQuestionNotFound() throws Exception {

        given(questionService.retrieveQuestionById(any(Long.class))).willReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testCreateAnswer() throws Exception {

        String json = JsonUtils.getJsonInput("answer/standard_answer");

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetAnswer() throws Exception {

        Answer answer = new Answer();
        answer.answerId(1L).answerText("foo");
        given(answerService.retrieveAnswerById(any(Long.class))).willReturn(Optional.of(answer));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertEquals(response, "{\"answerId\":1,\"answerText\":\"foo\"}");
    }
}
