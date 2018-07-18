package uk.gov.hmcts.reform.coh.controller;

import javassist.NotFoundException;
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
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.AnswerStateService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.states.AnswerStates;
import uk.gov.hmcts.reform.coh.states.QuestionStates;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    private static final String ENDPOINT = "/continuous-online-hearings/d9248584-4aa5-4cb0-aba6-d2633ad5a375/questions/d9248584-4aa5-4cb0-aba6-d2633ad5a375/answers";

    private AnswerRequest request;

    private Answer answer;

    private UUID uuid;
    private AnswerState answerState;

    private Question question;
    private QuestionState questionState;

    @Before
    public void setup() throws IOException, NotFoundException {
        answer = new Answer();
        uuid = UUID.fromString("399388b4-7776-40f9-bb79-0e900807063b");
        answer.answerId(uuid).answerText("foo");

        answerState = new AnswerState();
        answerState.setState(AnswerStates.DRAFTED.getStateName());
        answerState.setAnswerStateId(1);
        answer.setAnswerState(answerState);
        mockMvc = MockMvcBuilders.standaloneSetup(answerController).build();

        questionState = new QuestionState();
        questionState.setState(QuestionStates.ISSUED.getStateName());
        question = new Question();
        question.setQuestionState(questionState);

        given(questionService.retrieveQuestionById(any(UUID.class))).willReturn(Optional.of(question));
        given(answerService.retrieveAnswerById(any(UUID.class))).willReturn(Optional.ofNullable(answer));
        given(answerService.createAnswer(any(Answer.class))).willReturn(answer);
        given(answerService.updateAnswer(any(Answer.class), any(Answer.class))).willReturn(answer);
        given(answerStateService.retrieveAnswerStateByState(anyString())).willReturn(Optional.ofNullable(answerState));
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
    public void testEmptyAnswerTextForPatchThrowsRequestError() throws Exception {

        given(questionService.retrieveQuestionById(any(UUID.class))).willReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testEmptyAnswerState() throws Exception {
        AnswerRequest request = (AnswerRequest) JsonUtils.toObjectFromTestName("answer/standard_answer", AnswerRequest.class);
        request.setAnswerState(null);

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    public void testInvalidAnswerState() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        // Just pretend that DRAFTED isn't a valid state
        given(answerStateService.retrieveAnswerStateByState(AnswerStates.DRAFTED.getStateName())).willReturn(Optional.empty());

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
    public void testCreateAnswerInvalidQuestion() throws Exception {

        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(questionService.retrieveQuestionById(any(UUID.class))).willReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateMultipleAnswer() throws Exception {

        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(answerService.retrieveAnswersByQuestion(any(Question.class))).willReturn(Arrays.asList(answer));
        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    public void testGetAnswer() throws Exception {

        given(answerService.retrieveAnswerById(any(UUID.class))).willReturn(Optional.of(answer));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertEquals("{\"answerId\":\"" + uuid +"\",\"answer_text\":\"foo\",\"current_answer_state\":{\"state_name\":\"answer_drafted\"}}", response);
        Answer getAnswer = (Answer) JsonUtils.toObjectFromJson(response, Answer.class);
        assertEquals(uuid, getAnswer.getAnswerId());
        assertEquals("foo", getAnswer.getAnswerText());
        assertEquals("answer_drafted", getAnswer.getAnswerState().getState());
    }

    @Test
    public void testGetAnswers() throws Exception {

        AnswerState answerState = new AnswerState();
        Question question = new Question();
        question.setQuestionId(UUID.randomUUID());
        Answer answer = new Answer();
        answer.answerId(uuid).answerText("foo");
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
        given(answerService.retrieveAnswerById(any(UUID.class))).willReturn(Optional.of(answer));

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateAnswersFail() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(answerService.retrieveAnswerById(any(UUID.class))).willReturn(Optional.empty());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    public void testUpdateAnswersNonExistentQuestion() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(answerService.retrieveAnswerById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateAnswersWhenAnswerSubmitted() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        answerState.setState(AnswerStates.SUBMITTED.getStateName());
        answer.setAnswerState(answerState);
        given(answerService.retrieveAnswerById(any(UUID.class))).willReturn(Optional.of(answer));

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testUpdateAnswersFailDueToAnswerStateNotFoundThrowException() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(answerStateService.retrieveAnswerStateByState(anyString())).willReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    public void testUpdateAnswersFailDueToInvalidStateTransition() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(answerService.updateAnswer(any(Answer.class), any(Answer.class))).willThrow(NotFoundException.class);

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();
    }

    @Test
    public void testUpdateAnswersInvalidAnswerState() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(answerStateService.retrieveAnswerStateByState(anyString())).willReturn(Optional.empty());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();
        assertEquals("Answer state is not valid", result.getResponse().getContentAsString());
    }
}