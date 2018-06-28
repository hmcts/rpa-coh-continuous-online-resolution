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
import uk.gov.hmcts.reform.coh.domain.AnswerState;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.AnswerStateService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Mock
    private AnswerStateService answerStateService;

    @InjectMocks
    private AnswerController answerController;

    @Autowired
    private MockMvc mockMvc;

    private static final String ENDPOINT = "/online-hearings/d9248584-4aa5-4cb0-aba6-d2633ad5a375/questions/1/answers";

    private AnswerRequest request;

    private Answer answer;

    private AnswerState answerState;

    @Before
    public void setup() throws IOException {
        answer = new Answer();
        answer.answerId(1L).answerText("foo");

        answerState = new AnswerState();
        answerState.setState("DRAFTED");

        mockMvc = MockMvcBuilders.standaloneSetup(answerController).build();
        given(questionService.retrieveQuestionById(any(Long.class))).willReturn(new Question());
        given(answerService.createAnswer(any(Answer.class))).willReturn(answer);
        given(answerStateService.retrieveAnswerStateByState(any(String.class))).willReturn(Optional.of(answerState));
        request = (AnswerRequest) JsonUtils.toObjectFromTestName("answer/standard_answer", AnswerRequest.class);
    }

    @Test
    public void testEmptyAnswerText() throws Exception {

        request.setAnswerText(null);

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testEmptyAnswerState() throws Exception {
        AnswerRequest request = (AnswerRequest) JsonUtils.toObjectFromTestName("answer/standard_answer", AnswerRequest.class);
        request.setAnswerState(null);

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    public void testInvalidAnswerState() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        // Just pretend that DRAFTED isn't a valid state
        given(answerStateService.retrieveAnswerStateByState("DRAFTED")).willReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().is4xxClientError())
                .andReturn();
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
        answer.setAnswerState(answerState);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Answer getAnswer = (Answer) JsonUtils.toObjectFromJson(response, Answer.class);
        assertEquals(1L, getAnswer.getAnswerId().longValue());
        assertEquals("foo", getAnswer.getAnswerText());
        assertEquals("DRAFTED", getAnswer.getAnswerState().getState());
    }

    @Test
    public void testGetAnswers() throws Exception {

        AnswerState answerState = new AnswerState();
        Question question = new Question();
        question.setQuestionId(1L);
        Answer answer = new Answer();
        answer.answerId(1L).answerText("foo");
        answer.setAnswerState(answerState);
        List<Answer> answerList = new ArrayList<>();
        answerList.add(answer);
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

        mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }
}