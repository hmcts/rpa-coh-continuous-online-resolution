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
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionResponse;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.DecisionReplyResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.ConversationResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingResponse;
import uk.gov.hmcts.reform.coh.controller.question.QuestionResponse;
import uk.gov.hmcts.reform.coh.controller.utils.CohISO8601DateFormat;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.DecisionReplyService;
import uk.gov.hmcts.reform.coh.service.DecisionService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.states.AnswerStates;
import uk.gov.hmcts.reform.coh.states.DecisionsStates;
import uk.gov.hmcts.reform.coh.states.OnlineHearingStates;
import uk.gov.hmcts.reform.coh.states.QuestionStates;
import uk.gov.hmcts.reform.coh.util.QuestionEntityUtils;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.util.*;

import static java.util.Arrays.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local"})
public class ConversationsControllerTest {

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private DecisionService decisionService;

    @Mock
    private QuestionService questionService;

    @Mock
    private AnswerService answerService;

    @Mock
    private DecisionReplyService decisionReplyService;

    @InjectMocks
    private ConversationsController conversationController;

    @Autowired
    private MockMvc mockMvc;

    private static final String STARTED_STATE = OnlineHearingStates.STARTED.getStateName();

    private static final String ANSWER_DRAFTED = AnswerStates.DRAFTED.getStateName();

    private static final String uuid = "d9248584-4aa5-4cb0-aba6-d2633ad5a375";

    private static final String ENDPOINT = "/continuous-online-hearings/" + uuid + "/conversations";

    private OnlineHearing onlineHearing;

    private Decision decision;

    private Question question1;

    private Question question2;

    private UUID onlineHearingUuid;

    private DecisionState decisionState;

    private Date expiryDate;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(conversationController).build();

        onlineHearingUuid = UUID.fromString("d9248584-4aa5-4cb0-aba6-d2633ad5a375");
        onlineHearing = new OnlineHearing();
        onlineHearing.setCaseId("case_123");
        onlineHearing.setOnlineHearingId(onlineHearingUuid);
        onlineHearing.setStartDate(new Date());

        OnlineHearingState onlineHearingState = new OnlineHearingState();
        onlineHearingState.setState(STARTED_STATE);
        onlineHearing.setOnlineHearingState(onlineHearingState);

        OnlineHearingStateHistory ohHistory = new OnlineHearingStateHistory();
        ohHistory.setOnlinehearingstate(onlineHearingState);
        ohHistory.setDateOccurred(new Date());
        onlineHearing.setOnlineHearingStateHistories(asList(ohHistory));

        OnlineHearingPanelMember member = new OnlineHearingPanelMember();
        member.setFullName("foo bar");
        onlineHearing.setPanelMembers(asList(member));

        decisionState = new DecisionState();
        decisionState.setState("decision_drafted");

        expiryDate = new Date();
        decision = new Decision();
        decision.setDecisionId(UUID.randomUUID());
        decision.setOnlineHearing(onlineHearing);
        decision.setDecisionHeader("Decision header");
        decision.setDecisionText("Decision text");
        decision.setDecisionReason("Decision reason");
        decision.setDecisionAward("Decision award");
        decision.setDeadlineExpiryDate(expiryDate);
        decision.setDecisionstate(decisionState);

        DecisionStateHistory decisionStateHistory = new DecisionStateHistory();
        decisionStateHistory.setDateOccured(new Date());
        decisionStateHistory.setDecisionstate(decisionState);
        decision.setDecisionStateHistories(asList(decisionStateHistory));

        DecisionReply decisionReply = new DecisionReply();
        decisionReply.setDecision(decision);
        decisionReply.setDateOccured(new Date());
        decisionReply.setDecisionReply(true);
        decisionReply.setAuthorReferenceId("author ref id");
        decisionReply.setDecisionReplyReason("decision reply reason");
        decisionReply.setId(UUID.randomUUID());

        DecisionReply decisionReply2 = new DecisionReply();
        decisionReply2.setDecision(decision);
        decisionReply2.setDateOccured(new Date());
        decisionReply2.setDecisionReply(false);
        decisionReply2.setAuthorReferenceId("author ref id");
        decisionReply2.setDecisionReplyReason("decision reply reason");
        decisionReply2.setId(UUID.randomUUID());

        decision.setDecisionReplies(asList(decisionReply, decisionReply2));
        QuestionState issuedState = new QuestionState();
        issuedState.setState(QuestionStates.ISSUED.getStateName());

        question1 = QuestionEntityUtils.createTestQuestion(QuestionStates.ISSUED, onlineHearing);
        question2 = QuestionEntityUtils.createTestQuestion(QuestionStates.ISSUED, onlineHearing);

        Answer answer1 = new Answer();
        answer1.answerId(UUID.randomUUID()).answerText("foo");

        AnswerState answerState = new AnswerState();
        answerState.setState(ANSWER_DRAFTED);
        answerState.setAnswerStateId(1);
        answer1.setAnswerState(answerState);
        answer1.setQuestion(question1);

        AnswerStateHistory answerStateHistory = new AnswerStateHistory(answer1, answerState);
        answer1.setAnswerStateHistories(asList(answerStateHistory));

        when(onlineHearingService.retrieveOnlineHearing(onlineHearingUuid)).thenReturn(Optional.of(onlineHearing));
        when(decisionService.findByOnlineHearingId(onlineHearingUuid)).thenReturn(Optional.of(decision));
        when(questionService.findAllQuestionsByOnlineHearing(onlineHearing))
            .thenReturn(Optional.of(asList(question1, question2)));
        when(answerService.retrieveAnswersByQuestion(question1)).thenReturn(asList(answer1));
        when(answerService.retrieveAnswersByQuestion(question2)).thenReturn(asList(answer1));
        when(decisionReplyService.findAllDecisionReplyByDecision(decision)).thenReturn(asList(decisionReply));
    }

    @Test
    public void testOnlineHearingNotFound() throws Exception {
        when(onlineHearingService.retrieveOnlineHearing(any(UUID.class))).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .content(""))
            .andExpect(status().isNotFound());
    }

    @Test
    public void testDecisionNotFoundQuestionsNotFound() throws Exception {
        when(decisionService.findByOnlineHearingId(onlineHearingUuid)).thenReturn(Optional.empty());
        when(questionService.findAllQuestionsByOnlineHearing(onlineHearing)).thenReturn(Optional.empty());

        ConversationResponse response = submitGet();
        assertOnlineHearing(response);
        assertNull(response.getOnlineHearing().getDecisionResponse());
        assertNull(response.getOnlineHearing().getQuestions());
    }

    @Test
    public void testDecisionFound() throws Exception {
        ConversationResponse response = submitGet();
        assertDecision(response);
    }

    @Test
    public void testDecisionRepliesFound() throws Exception {
        ConversationResponse response = submitGet();
        assertDecisionReplies(response);
    }

    @Test
    public void testDecisionRepliesNotFound() throws Exception {
        when(decisionReplyService.findAllDecisionReplyByDecision(decision)).thenReturn(Collections.emptyList());
        ConversationResponse response = submitGet();
        assertQuestionWithoutDecisionReplies(response);
    }

    @Test
    public void testAnswersNotFound() throws Exception {
        when(answerService.retrieveAnswersByQuestion(question1)).thenReturn(Collections.emptyList());
        when(answerService.retrieveAnswersByQuestion(question2)).thenReturn(Collections.emptyList());

        ConversationResponse response = submitGet();
        assertQuestionWithoutAnswer(response);
    }

    @Test
    public void testQuestionWithAnAnswers() throws Exception {
        ConversationResponse response = submitGet();
        assertQuestionswithAnswers(response);
    }

    @Test
    public void testConversation() throws Exception {
        ConversationResponse response = submitGet();
        assertOnlineHearing(response);
        assertDecision(response);
        assertDecisionReplies(response);
        assertQuestionswithAnswers(response);
    }

    private ConversationResponse submitGet() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get((ENDPOINT))
            .contentType(MediaType.APPLICATION_JSON)
            .content(""))
            .andExpect(status().isOk())
            .andReturn();

        return JsonUtils.toObjectFromJson(result.getResponse().getContentAsString(), ConversationResponse.class);
    }

    private void assertOnlineHearing(ConversationResponse response) {
        OnlineHearingResponse ohResponse = response.getOnlineHearing();
        assertNotNull(ohResponse);
        assertEquals("/continuous-online-hearings/" + onlineHearingUuid, response.getOnlineHearing().getUri());
        assertEquals("case_123", ohResponse.getCaseId());
        assertEquals(STARTED_STATE, ohResponse.getCurrentState().getName());
        assertNotNull(ohResponse.getCurrentState().getDatetime());
        assertEquals(1, ohResponse.getPanel().size());
        assertEquals("foo bar", ohResponse.getPanel().get(0).getName());
        assertEquals(1, ohResponse.getHistories().size());
    }

    private void assertDecision(ConversationResponse response) {
        DecisionResponse decisionResponse = response.getOnlineHearing().getDecisionResponse();
        assertNotNull(decisionResponse);
        assertEquals("/continuous-online-hearings/" + onlineHearingUuid + "/decisions", decisionResponse.getUri());
        assertNotNull(decision.getDecisionId());
        assertEquals(onlineHearingUuid.toString(), decisionResponse.getOnlineHearingId());
        assertEquals("Decision header", decisionResponse.getDecisionHeader());
        assertEquals("Decision text", decisionResponse.getDecisionText());
        assertEquals("Decision reason", decisionResponse.getDecisionReason());
        assertEquals("Decision award", decisionResponse.getDecisionAward());
        assertEquals(CohISO8601DateFormat.format(expiryDate), decisionResponse.getDeadlineExpiryDate());
        assertEquals(decisionState.getState(), decisionResponse.getDecisionState().getName());
        assertNotNull(decisionResponse.getDecisionState().getDatetime());
    }

    private void assertDecisionReplies(ConversationResponse response) {
        List<DecisionReplyResponse> decisionReplyResponses = response.getOnlineHearing().getDecisionResponse()
            .getDecisionReplyResponses();
        assertFalse(decisionReplyResponses.isEmpty());
        assertEquals(1, decisionReplyResponses.size());

        int n = 0;
        for (DecisionReplyResponse replyResponse : decisionReplyResponses) {
            DecisionReply expectedReply = decision.getDecisionReplies().get(n);

            String replyState =
                expectedReply.getDecisionReply()
                    ? DecisionsStates.DECISIONS_ACCEPTED.getStateName()
                    : DecisionsStates.DECISIONS_REJECTED.getStateName();

            assertEquals(replyState, replyResponse.getDecisionReply());
            assertEquals(CohISO8601DateFormat.format(expectedReply.getDateOccured()),
                replyResponse.getDecisionReplyDate());
            assertEquals(expectedReply.getDecision().getDecisionId().toString(), replyResponse.getDecisionId());
            assertEquals(expectedReply.getAuthorReferenceId(), replyResponse.getAuthorReference());
            assertEquals(expectedReply.getId().toString(), replyResponse.getDecisionReplyId());
            assertEquals(expectedReply.getDecisionReplyReason(), replyResponse.getDecisionReplyReason());
            n++;
        }
    }

    private void assertQuestionWithoutDecisionReplies(ConversationResponse response) {
        List<DecisionReplyResponse> decisionReplyResponses = response.getOnlineHearing().getDecisionResponse()
            .getDecisionReplyResponses();
        assertNull(decisionReplyResponses);
    }

    private void assertQuestionswithAnswers(ConversationResponse response) {
        assertQuestions(response, true);
    }

    private void assertQuestionWithoutAnswer(ConversationResponse response) {
        assertQuestions(response, false);
    }

    private void assertQuestions(ConversationResponse response, boolean withAnswers) {
        List<QuestionResponse> questions = response.getOnlineHearing().getQuestions();

        assertNotNull(questions);
        assertEquals(2, questions.size());
        for (QuestionResponse question : questions) {
            if (withAnswers) {
                assertNotNull(question.getAnswers());
                assertEquals(1, question.getAnswers().size());
                AnswerResponse answerResponse = question.getAnswers().get(0);
                assertEquals("foo", answerResponse.getAnswerText());
                assertEquals(ANSWER_DRAFTED, answerResponse.getStateResponse().getName());
                assertNotNull(answerResponse.getStateResponse().getDatetime());
                assertEquals(1, answerResponse.getHistories().size());
                assertEquals(ANSWER_DRAFTED, answerResponse.getHistories().get(0).getName());
                assertEquals(answerResponse.getStateResponse().getDatetime(),
                    answerResponse.getHistories().get(0).getDatetime());
            } else {
                assertNull(question.getAnswers());
            }
        }
    }
}
