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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerRequest;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local"})
public class AnswerControllerTest {

    @Mock
    private QuestionService questionService;

    @Mock
    private AnswerService answerService;

    @InjectMocks
    private AnswerController answerController;

    @Autowired
    private MockMvc mockMvc;

    private static final String ENDPOINT = "/online-hearings/d9248584-4aa5-4cb0-aba6-d2633ad5a375/questions/d9248584-4aa5-4cb0-aba6-d2633ad5a375/answers";

    private AnswerRequest request;

    private Answer answer;

    @Before
    public void setup() throws IOException {
        mockMvc = MockMvcBuilders.standaloneSetup(answerController).build();
        given(questionService.retrieveQuestionById(any(UUID.class))).willReturn(Optional.of(new Question()));
        given(answerService.createAnswer(any(Answer.class))).willReturn(new Answer());
        request = (AnswerRequest) JsonUtils.toObjectFromTestName("answer/standard_answer", AnswerRequest.class);
        answer = new Answer();
        answer.answerId(1L).answerText("foo");
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

        given(questionService.retrieveQuestionById(any(UUID.class))).willReturn(null);

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

        given(answerService.retrieveAnswerById(any(Long.class))).willReturn(Optional.of(answer));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertEquals(response, "{\"answerId\":1,\"answerText\":\"foo\"}");
    }

    @Test
    public void testGetAnswers() throws Exception {

        Question question = new Question();
        question.setQuestionId(UUID.randomUUID());
        Answer answer = new Answer();
        answer.answerId(1L).answerText("foo");
        List<Answer> answerList = new ArrayList<>();
        answerList.add(answer);
        given(questionService.retrieveQuestionById(any(UUID.class))).willReturn(Optional.of(question));
        given(answerService.retrieveAnswersByQuestion(question)).willReturn(answerList);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Answer [] answers = (Answer[]) JsonUtils.toObjectFromJson(response, Answer[].class);
        assertEquals(1, answers.length);
    }

    @Test
    public void testUpdateAnswers() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(answerService.retrieveAnswerById(any(Long.class))).willReturn(Optional.of(answer));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("{\"answerId\":1}", result.getResponse().getContentAsString());
    }

    @Test
    public void testUpdateAnswersFail() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(answerService.retrieveAnswerById(any(Long.class))).willReturn(Optional.empty());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }
}