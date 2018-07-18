package uk.gov.hmcts.reform.coh.task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingStates;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.states.AnswerStates;

import java.util.Arrays;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class AnswersReceivedTaskTest {

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private OnlineHearingStateService onlineHearingStateService;

    @Mock
    private QuestionService questionService;

    @Mock
    private AnswerService answerService;

    @InjectMocks
    private AnswersReceivedTask answersReceivedTask;

    private OnlineHearingState onlineHearingState;

    private OnlineHearing onlineHearing;

    private Question question;

    private AnswerState answerState;

    private Answer answer;

    @Before
    public void setup() {

        onlineHearingState = new OnlineHearingState();
        onlineHearingState.setState(OnlineHearingStates.ANSWERS_SENT.getStateName());

        onlineHearing = new OnlineHearing();
        question = new Question();

        answerState = new AnswerState();
        answerState.setState(AnswerStates.SUBMITTED.getStateName());
        answer = new Answer();
        answer.setAnswerState(answerState);
        given(onlineHearingStateService.retrieveOnlineHearingStateByState( OnlineHearingStates.ANSWERS_SENT.getStateName())).willReturn(Optional.ofNullable(onlineHearingState));
        given(questionService.finaAllQuestionsByOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.of(Arrays.asList(question)));
        given(answerService.retrieveAnswersByQuestion(question)).willReturn(Arrays.asList(answer));
    }

    @Test
    public void testOnlineHearingContainsNoQuestions() {
        given(questionService.finaAllQuestionsByOnlineHearing(onlineHearing)).willReturn(Optional.empty());
        answersReceivedTask.execute(onlineHearing);
        verify(questionService, times(1)).finaAllQuestionsByOnlineHearing(onlineHearing);
    }

    @Test
    public void testOnlineHearingContainsQuestionsNoAnswers() {
        answerState.setState(AnswerStates.DRAFTED.getStateName());
        assertFalse(answersReceivedTask.questionContainsAnAnswer(question));
    }

    @Test
    public void testOnlineHearingContainsQuestionsIsAnswered() {
        assertTrue(answersReceivedTask.questionContainsAnAnswer(question));
    }

    @Test
    public void testOnlineHearingContainsQuestionsIsNotAnswered() {
        given(answerService.retrieveAnswersByQuestion(question)).willReturn(Arrays.asList());
        assertFalse(answersReceivedTask.questionContainsAnAnswer(question));
    }

    @Test
    public void testOnlineHearingAnswerSentStateNotFound() {
        answersReceivedTask.execute(onlineHearing);
        verify(onlineHearingStateService, times(1)).retrieveOnlineHearingStateByState(OnlineHearingStates.ANSWERS_SENT.getStateName());
    }

    @Test
    public void testOnlineHearingSetToAnswerSentState() {
        answersReceivedTask.execute(onlineHearing);
        assertEquals(OnlineHearingStates.ANSWERS_SENT.getStateName(), onlineHearing.getOnlineHearingState().getState());
    }
}
