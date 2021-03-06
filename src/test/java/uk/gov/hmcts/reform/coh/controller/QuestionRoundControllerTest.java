package uk.gov.hmcts.reform.coh.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundResponse;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundsResponse;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.service.*;
import uk.gov.hmcts.reform.coh.states.QuestionStates;
import uk.gov.hmcts.reform.coh.util.OnlineHearingEntityUtils;
import uk.gov.hmcts.reform.coh.util.QuestionEntityUtils;
import uk.gov.hmcts.reform.coh.util.QuestionStateUtils;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local"})
public class QuestionRoundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private QuestionRoundService questionRoundService;

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private QuestionStateService questionStateService;

    @Mock
    private SessionEventService sessionEventService;

    @Mock
    private AnswerService answerService;

    @InjectMocks
    private QuestionRoundController questionRoundController;

    private UUID cohId;

    private final int ROUND_ID = 1;
    private QuestionRound questionRound;
    private QuestionState issuedState;
    private QuestionState issuePendingState;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(questionRoundController).build();

        List<QuestionRound> questionRounds = new ArrayList<>();
        questionRound = new QuestionRound();
        questionRound.setQuestionRoundNumber(ROUND_ID);
        QuestionRoundState questionRoundState = new QuestionRoundState();

        issuedState = QuestionStateUtils.get(QuestionStates.ISSUED);

        issuePendingState = QuestionStateUtils.get(QuestionStates.ISSUE_PENDING);

        QuestionState draftedState = QuestionStateUtils.get(QuestionStates.DRAFTED);
        questionRoundState.setState(draftedState);

        List<Question> questions = new ArrayList<>();
        Question question = QuestionEntityUtils.createTestQuestion(QuestionStates.ISSUE_PENDING);
        question.setQuestionRound(ROUND_ID);
        questions.add(question);
        questionRound.setQuestionList(questions);

        questionRound.setQuestionRoundState(questionRoundState);
        questionRounds.add(questionRound);

        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setMaxQuestionRounds(3);

        OnlineHearing onlineHearing = OnlineHearingEntityUtils.createTestOnlineHearingEntity();
        cohId = onlineHearing.getOnlineHearingId();
        onlineHearing.setJurisdiction(jurisdiction);

        given(questionStateService.retrieveQuestionStateByStateName(anyString())).willReturn(Optional.of(issuePendingState));
        given(questionRoundService.issueQuestionRound(any(OnlineHearing.class), any(QuestionState.class), anyInt())).willReturn(null);
        given(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.of(onlineHearing));
        given(questionRoundService.getAllQuestionRounds(any(OnlineHearing.class))).willReturn(questionRounds);
        given(questionRoundService.getCurrentQuestionRoundNumber(any(OnlineHearing.class))).willReturn(2);
        given(questionRoundService.getNextQuestionRound(any(OnlineHearing.class), anyInt())).willReturn(3);
        given(questionRoundService.getPreviousQuestionRound(anyInt())).willReturn(1);
        given(questionRoundService.getQuestionRoundByRoundId(any(OnlineHearing.class), anyInt())).willReturn(questionRound);
        given(sessionEventService.createSessionEvent(any(OnlineHearing.class), anyString())).willReturn(new SessionEvent());

        given(questionRoundService.retrieveQuestionRoundState(any(QuestionRound.class))).willReturn(new QuestionRoundState(draftedState));
        given(questionRoundService.getCurrentQuestionRoundNumber(any(OnlineHearing.class))).willReturn(1);
        doReturn(false).when(questionRoundService).alreadyIssued(any(QuestionRoundState.class));
    }

    @Test
    public void testGetAllQuestionRounds() throws Exception {
        given(questionRoundService.getCurrentQuestionRoundNumber(any(OnlineHearing.class))).willReturn(2);
        given(answerService.retrieveAnswersByQuestion(any(Question.class))).willReturn((Collections.emptyList()));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(CohUriBuilder.buildQuestionRoundGetAll(cohId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        QuestionRoundsResponse questionRoundsResponse = JsonUtils.toObjectFromJson(response, QuestionRoundsResponse.class);
        assertEquals(3, (int) questionRoundsResponse.getMaxQuestionRound());
        assertEquals(2, (int) questionRoundsResponse.getCurrentQuestionRound());
        assertEquals(1, (int) questionRoundsResponse.getPreviousQuestionRound());
    }

    @Test
    public void testOnlineHearingNotFound() throws Exception {
        given(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.get(CohUriBuilder.buildQuestionRoundGetAll(cohId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void testGetAQuestionRound() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(CohUriBuilder.buildQuestionRoundGet(cohId, ROUND_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        QuestionRoundResponse questionRoundResponse = JsonUtils.toObjectFromJson(response, QuestionRoundResponse.class);
        assertEquals(1, questionRoundResponse.getQuestionList().size());
        assertEquals("1", questionRoundResponse.getQuestionRound());
    }

    @Test
    public void testGetAQuestionRoundWithNoQuestions() throws Exception {
        questionRound.setQuestionList(new ArrayList<>());

        given(questionRoundService.getQuestionRoundByRoundId(any(OnlineHearing.class), anyInt())).willReturn(questionRound);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(CohUriBuilder.buildQuestionRoundGet(cohId, ROUND_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        QuestionRoundResponse questionRoundResponse = JsonUtils.toObjectFromJson(response, QuestionRoundResponse.class);
        assertEquals(0, questionRoundResponse.getQuestionList().size());
        assertEquals("1", questionRoundResponse.getQuestionRound());
    }

    @Test
    public void testUpdateANonExistingQuestionRoundThrowsNotFound() throws Exception {
        given(questionRoundService.getCurrentQuestionRoundNumber(any(OnlineHearing.class))).willReturn(2);

        String json = JsonUtils.getJsonInput("question_round/issue_question_round");
        mockMvc.perform(MockMvcRequestBuilders.put(CohUriBuilder.buildQuestionRoundGet(cohId, 3))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void testUpdateQuestionRoundWithNonExistingOnlineHearingThrowsNotFound() throws Exception {
        given(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.empty());
        String json = JsonUtils.getJsonInput("question_round/issue_question_round");
        mockMvc.perform(MockMvcRequestBuilders.put(CohUriBuilder.buildQuestionRoundGet(cohId, ROUND_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void testUpdateQuestionRoundWithNonExistingStateThrowsBadRequest() throws Exception {
        given(questionStateService.retrieveQuestionStateByStateName(anyString())).willReturn(Optional.empty());
        String json = JsonUtils.getJsonInput("question_round/issue_question_round");
        mockMvc.perform(MockMvcRequestBuilders.put(CohUriBuilder.buildQuestionRoundGet(cohId, ROUND_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void testUpdateQuestionRoundWithStateOtherThanIssuedThrowsBadRequest() throws Exception {
        QuestionState draftedState = new QuestionState();
        draftedState.setState("DRAFTED");
        draftedState.setQuestionStateId(1);

        given(questionStateService.retrieveQuestionStateByStateName(anyString())).willReturn(Optional.of(draftedState));
        String json = JsonUtils.toJson("question_round/issue_question_round");
        mockMvc.perform(MockMvcRequestBuilders.put(CohUriBuilder.buildQuestionRoundGet(cohId, ROUND_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void testUpdatePreviousQuestionRoundThrowsBadRequest() throws Exception {
        given(questionRoundService.getCurrentQuestionRoundNumber(any(OnlineHearing.class))).willReturn(2);

        String json = JsonUtils.getJsonInput("question_round/issue_question_round");
        mockMvc.perform(MockMvcRequestBuilders.put(CohUriBuilder.buildQuestionRoundGet(cohId, ROUND_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();
    }

    @Test
    public void testUpdateCurrentQuestionRoundToIssued() throws Exception {
        given(questionStateService.retrieveQuestionStateByStateName(anyString())).willReturn(Optional.of(issuePendingState));

        String json = JsonUtils.getJsonInput("question_round/issue_question_round");
        mockMvc.perform(MockMvcRequestBuilders.put(CohUriBuilder.buildQuestionRoundGet(cohId, ROUND_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }


    @Test
    public void testReissuingTheCurrentQuestionThrowsNotAValidUpdate() throws Exception {
        doReturn(new QuestionRoundState(issuedState)).when(questionRoundService).retrieveQuestionRoundState(any(QuestionRound.class));
        doReturn(true).when(questionRoundService).alreadyIssued(any(QuestionRoundState.class));
        String json = JsonUtils.getJsonInput("question_round/issue_question_round");
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(CohUriBuilder.buildQuestionRoundGet(cohId, ROUND_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().is4xxClientError())
                .andReturn();
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
    }
}
