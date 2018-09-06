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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.coh.controller.question.*;
import uk.gov.hmcts.reform.coh.controller.utils.CohISO8601DateFormat;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.controller.validators.LinkedQuestionValidator;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.service.QuestionStateService;
import uk.gov.hmcts.reform.coh.states.QuestionStates;
import uk.gov.hmcts.reform.coh.util.OnlineHearingEntityUtils;
import uk.gov.hmcts.reform.coh.util.QuestionEntityUtils;
import uk.gov.hmcts.reform.coh.util.QuestionStateUtils;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local"})
public class QuestionControllerTest {

    @Mock
    private QuestionService questionService;

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private QuestionStateService questionStateService;

    @Mock
    private AnswerService answerService;

    @InjectMocks
    private QuestionController questionController;

    @Autowired
    private LinkedQuestionValidator questionValidator;

    @Autowired
    private MockMvc mockMvc;

    private QuestionRequest questionRequest;

    private OnlineHearing onlineHearing;

    private Question question;

    private QuestionState issuedState;

    private QuestionState draftedState;

    private Date today;

    @Before
    public void setup() throws IOException {
        questionRequest = JsonUtils.toObjectFromTestName("question/standard_question", QuestionRequest.class);
        onlineHearing = OnlineHearingEntityUtils.createTestOnlineHearingEntity();
        question = QuestionEntityUtils.createTestQuestion();
        question.setOnlineHearing(onlineHearing);

        AnswerState answerState = new AnswerState();
        answerState.setState("foo");
        Answer answer = new Answer();
        answer.setAnswerId(UUID.randomUUID());
        answer.setAnswerText("test answer");
        answer.setQuestion(question);
        answer.setAnswerState(answerState);
        List<Answer> answerList = new ArrayList<>();
        answerList.add(answer);

        issuedState = QuestionStateUtils.get(QuestionStates.ISSUED);
        draftedState = QuestionStateUtils.get(QuestionStates.DRAFTED);

        question.setQuestionState(issuedState);

        List<QuestionStateHistory> histories = new ArrayList<>();
        today = new Date();
        QuestionStateHistory history = new QuestionStateHistory(question, issuedState);
        history.setDateOccurred(today);
        histories.add(history);
        question.setQuestionStateHistories(histories);

        mockMvc = MockMvcBuilders.standaloneSetup(questionController).build();
        questionController.setQuestionValidator(questionValidator);
        given(questionService.retrieveQuestionById(question.getQuestionId())).willReturn(Optional.of(question));
        given(questionService.createQuestion(any(Question.class), any(OnlineHearing.class))).willReturn(question);
        given(questionStateService.retrieveQuestionStateByStateName(anyString())).willReturn(Optional.of(issuedState));
        willDoNothing().given(questionService).updateQuestion(any(Question.class));
        given(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.of(onlineHearing));
        given(answerService.retrieveAnswersByQuestion(any(Question.class))).willReturn(answerList);
    }

    @Test
    public void testGetQuestion() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(CohUriBuilder.buildQuestionGet(onlineHearing.getOnlineHearingId(), question.getQuestionId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        QuestionResponse responseQuestion = JsonUtils.toObjectFromJson(response, QuestionResponse.class);
        assertEquals(question.getQuestionId().toString(), responseQuestion.getQuestionId());
        assertEquals(question.getQuestionHeaderText(), responseQuestion.getQuestionHeaderText());
        assertEquals(question.getQuestionText(), responseQuestion.getQuestionBodyText());
        assertEquals(question.getQuestionRound().toString(), responseQuestion.getQuestionRound());
        assertEquals(Integer.toString(question.getQuestionOrdinal()), responseQuestion.getQuestionOrdinal());
        assertEquals(issuedState.getState(), responseQuestion.getCurrentState().getName());
        assertEquals(CohISO8601DateFormat.format(today), responseQuestion.getCurrentState().getDatetime());
    }

    @Test
    public void testGetQuestionUnknownQuestionId() throws Exception {

        given(questionService.retrieveQuestionById(question.getQuestionId())).willReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.get(CohUriBuilder.buildQuestionGet(onlineHearing.getOnlineHearingId(), question.getQuestionId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateQuestionAndCheckLocationIsReturnedInHeader() throws Exception {

        String json = JsonUtils.getJsonInput("question/standard_question");
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(CohUriBuilder.buildQuestionPost(onlineHearing.getOnlineHearingId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        CreateQuestionResponse responseQuestion = JsonUtils.toObjectFromJson(response, CreateQuestionResponse.class);
        assertNotNull(responseQuestion.getQuestionId());

        String returnedUrl = result.getResponse().getHeader("Location");
        try {
            URL u = new URL(returnedUrl); // this would check for the protocol
            u.toURI(); // does the extra checking required for validation of URI
            assertTrue(true);
        } catch(MalformedURLException e){
            fail();
        }
    }

    @Test
    public void testCreateQuestionInvalidOnlineHearing() throws Exception {

        given(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.empty());

        String json = JsonUtils.getJsonInput("question/standard_question");
        mockMvc.perform(MockMvcRequestBuilders.post(CohUriBuilder.buildQuestionPost(onlineHearing.getOnlineHearingId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testValidateQuestionRound() throws Exception {

        questionRequest.setQuestionRound(null);
        mockMvc.perform(MockMvcRequestBuilders.post(CohUriBuilder.buildQuestionPost(onlineHearing.getOnlineHearingId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(questionRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testValidateQuestionOrdinal() throws Exception {

        questionRequest.setQuestionOrdinal(null);
        mockMvc.perform(MockMvcRequestBuilders.post(CohUriBuilder.buildQuestionPost(onlineHearing.getOnlineHearingId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(questionRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testValidateQuestionHeaderText() throws Exception {

        questionRequest.setQuestionHeaderText(null);
        mockMvc.perform(MockMvcRequestBuilders.post(CohUriBuilder.buildQuestionPost(onlineHearing.getOnlineHearingId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(questionRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testValidateQuestionBodyText() throws Exception {

        questionRequest.setQuestionBodyText(null);
        mockMvc.perform(MockMvcRequestBuilders.post(CohUriBuilder.buildQuestionPost(onlineHearing.getOnlineHearingId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(questionRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testValidateQuestionOwnerReference() throws Exception {

        questionRequest.setOwnerReference(null);
        mockMvc.perform(MockMvcRequestBuilders.post(CohUriBuilder.buildQuestionPost(onlineHearing.getOnlineHearingId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(questionRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testGetAllQuestions() throws Exception {

        List<Question> responses = new ArrayList<>();
        responses.add(question);

        given(questionService.findAllQuestionsByOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.of(responses));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(CohUriBuilder.buildQuestionPost(onlineHearing.getOnlineHearingId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(questionRequest)))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        AllQuestionsResponse questionResponses = mapper.readValue(result.getResponse().getContentAsString(), AllQuestionsResponse.class);

        assertEquals(1, questionResponses.getQuestions().size());
        assertEquals("test answer", questionResponses.getQuestions().get(0).getAnswers().get(0).getAnswerText());
    }

    @Test
    public void testGetAllQuestionsNone() throws Exception {

        given(questionService.findAllQuestionsByOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.ofNullable(null));
        mockMvc.perform(MockMvcRequestBuilders.get(CohUriBuilder.buildQuestionPost(onlineHearing.getOnlineHearingId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(questionRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllQuestionsWhenNone() throws Exception {

        List<Question> responses = new ArrayList<>();

        given(questionService.findAllQuestionsByOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.ofNullable(responses));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(CohUriBuilder.buildQuestionPost(onlineHearing.getOnlineHearingId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(questionRequest)))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        AllQuestionsResponse questionResponses = mapper.readValue(result.getResponse().getContentAsString(), AllQuestionsResponse.class);

        assertEquals(0, questionResponses.getQuestions().size());
    }

    @Test
    public void testEditQuestion() throws Exception {
        String json = JsonUtils.getJsonInput("question/update_question");

        mockMvc.perform(MockMvcRequestBuilders.put(CohUriBuilder.buildQuestionGet(onlineHearing.getOnlineHearingId(), question.getQuestionId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testEditQuestionWhenQuestionNotFound() throws Exception {
        String json = JsonUtils.getJsonInput("question/update_question");
        given(questionService.retrieveQuestionById(question.getQuestionId())).willReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.put(CohUriBuilder.buildQuestionGet(onlineHearing.getOnlineHearingId(), question.getQuestionId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void testEditQuestionNotAssignedToOnlineHearingBadRequest() throws Exception {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(UUID.randomUUID());
        question.setOnlineHearing(onlineHearing);
        String json = JsonUtils.getJsonInput("question/update_question");
        mockMvc.perform(MockMvcRequestBuilders.put(CohUriBuilder.buildQuestionGet(UUID.randomUUID(), question.getQuestionId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void testEditQuestionToStateIssuedUnprocessableEntity() throws Exception {
        String json = JsonUtils.getJsonInput("question/update_question");
        UpdateQuestionRequest updateQuestionRequest = JsonUtils.toObjectFromJson(json, UpdateQuestionRequest.class);
        updateQuestionRequest.setQuestionState(QuestionStates.ISSUED.getStateName());
        json = JsonUtils.toJson(updateQuestionRequest);
        mockMvc.perform(MockMvcRequestBuilders.put(CohUriBuilder.buildQuestionGet(onlineHearing.getOnlineHearingId(), question.getQuestionId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();
    }

    @Test
    public void testEditQuestionStateNotValidUnprocessableEntityt() throws Exception {
        String json = JsonUtils.getJsonInput("question/update_question");
        given(questionStateService.retrieveQuestionStateByStateName(anyString())).willReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.put(CohUriBuilder.buildQuestionGet(onlineHearing.getOnlineHearingId(), question.getQuestionId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();
    }

    @Test
    public void testDeleteQuestion() throws Exception {
        question.setQuestionState(draftedState);
        given(questionStateService.retrieveQuestionStateByStateName(anyString())).willReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.delete(CohUriBuilder.buildQuestionGet(onlineHearing.getOnlineHearingId(), question.getQuestionId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteQuestionNonExistentOnlineHearing() throws Exception {
        question.setQuestionState(draftedState);
        given(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.delete(CohUriBuilder.buildQuestionGet(onlineHearing.getOnlineHearingId(), question.getQuestionId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteQuestionNonExistentQuestion() throws Exception {
        question.setQuestionState(draftedState);
        given(questionService.retrieveQuestionById(any(UUID.class))).willReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.delete(CohUriBuilder.buildQuestionGet(onlineHearing.getOnlineHearingId(), question.getQuestionId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteQuestionNotInDraftState() throws Exception {
        given(questionService.retrieveQuestionById(any(UUID.class))).willReturn(Optional.ofNullable(question));
        mockMvc.perform(MockMvcRequestBuilders.delete(CohUriBuilder.buildQuestionGet(onlineHearing.getOnlineHearingId(), question.getQuestionId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isUnprocessableEntity());
    }
}