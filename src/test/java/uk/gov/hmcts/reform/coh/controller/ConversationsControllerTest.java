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
import uk.gov.hmcts.reform.coh.controller.question.QuestionResponse;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.DecisionService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.states.AnswerStates;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import java.util.*;

import static java.util.Arrays.*;
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

    @InjectMocks
    private ConversationsController conversationController;

    @Autowired
    private MockMvc mockMvc;

    private static final String uuid = "d9248584-4aa5-4cb0-aba6-d2633ad5a375";

    private static final String ENDPOINT = "/continuous-online-hearings/" + uuid + "/conversations";

    private OnlineHearing onlineHearing;

    private OnlineHearingPanelMember member;

    private OnlineHearingState onlineHearingState;

    private Decision decision;

    private QuestionState issuedState;

    private Question question1;

    private Answer answer1;

    private AnswerState answerState;

    private Question question2;

    private UUID onlineHearingUuid;

    private DecisionState decisionState;

    private Date expiryDate;

    @Before
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(conversationController).build();

        onlineHearingUuid = UUID.fromString("d9248584-4aa5-4cb0-aba6-d2633ad5a375");
        onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingUuid);
        onlineHearing.setStartDate(new Date());

        onlineHearingState = new OnlineHearingState();
        onlineHearingState.setState("continuous_online_hearing_started");
        onlineHearing.setOnlineHearingState(onlineHearingState);

        member = new OnlineHearingPanelMember();
        member.setFullName("foo bar");
        onlineHearing.setPanelMembers(asList(member));

        decisionState = new DecisionState();
        decisionState.setState("decision_drafted");

        expiryDate = new Date();
        decision = new Decision();
        decision.setDecisionId(UUID.randomUUID());
        decision.setOnlineHearing(onlineHearing);
        decision.setDecisionHeader("Decision header");
        decision.setDecisionText("Decision test");
        decision.setDecisionReason("Decision reason");
        decision.setDecisionAward("Decision award");
        decision.setDeadlineExpiryDate(expiryDate);
        decision.setDecisionstate(decisionState);

        issuedState = new QuestionState();
        issuedState.setState(QuestionStates.ISSUED.getStateName());

        question1 = new Question();
        question1.setQuestionId(UUID.randomUUID());
        question1.setQuestionHeaderText("foo");
        question1.setQuestionText("bar");
        question1.setOnlineHearing(onlineHearing);
        question1.setQuestionRound(1);
        question1.setQuestionOrdinal(1);
        question1.setQuestionState(issuedState);

        question2 = new Question();
        question2.setQuestionId(UUID.randomUUID());
        question2.setQuestionHeaderText("foo");
        question2.setQuestionText("bar");
        question2.setOnlineHearing(onlineHearing);
        question2.setQuestionRound(1);
        question2.setQuestionOrdinal(1);
        question2.setQuestionState(issuedState);

        answer1 = new Answer();
        answer1.answerId(UUID.randomUUID()).answerText("foo");

        answerState = new AnswerState();
        answerState.setState(AnswerStates.DRAFTED.getStateName());
        answerState.setAnswerStateId(1);
        answer1.setAnswerState(answerState);

        when(onlineHearingService.retrieveOnlineHearing(onlineHearingUuid)).thenReturn(Optional.of(onlineHearing));
        when(decisionService.findByOnlineHearingId(onlineHearingUuid)).thenReturn(Optional.of(decision));
        when(questionService.findAllQuestionsByOnlineHearing(onlineHearing)).thenReturn(Optional.of(asList(question1, question2)));
        when(answerService.retrieveAnswersByQuestion(question1)).thenReturn(Arrays.asList(answer1));
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
    public void testDecisionNotFoundQuestionsNotFOund() throws Exception {
        when(decisionService.findByOnlineHearingId(onlineHearingUuid)).thenReturn(Optional.empty());
        when(questionService.findAllQuestionsByOnlineHearing(onlineHearing)).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.get((ENDPOINT))
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk());

    }

    @Test
    public void testAnswersNotFound() throws Exception {
        when(answerService.retrieveAnswersByQuestion(question1)).thenReturn(Collections.emptyList());
        mockMvc.perform(MockMvcRequestBuilders.get((ENDPOINT))
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk());
    }

    @Test
    public void testConversation() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get((ENDPOINT))
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println(result.getResponse().getContentAsString());
    }
}
