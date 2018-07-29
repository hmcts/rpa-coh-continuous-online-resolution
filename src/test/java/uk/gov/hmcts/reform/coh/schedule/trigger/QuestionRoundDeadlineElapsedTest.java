package uk.gov.hmcts.reform.coh.schedule.trigger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.service.QuestionStateService;
import uk.gov.hmcts.reform.coh.service.SessionEventService;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.coh.states.QuestionStates.DEADLINE_ELAPSED;
import static uk.gov.hmcts.reform.coh.states.QuestionStates.ISSUE_PENDING;

@RunWith(SpringRunner.class)
public class QuestionRoundDeadlineElapsedTest {

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private QuestionService questionService;

    @Mock
    private QuestionStateService stateService;

    @Mock
    private SessionEventService sessionEventService;

    @InjectMocks
    private QuestionRoundDeadlineElapsed trigger;

    private OnlineHearing onlineHearing;

    private Question question;

    private QuestionState pending;

    private QuestionState elapsed;

    @Before
    public void setUp() {
        pending = new QuestionState();
        pending.setState(ISSUE_PENDING.getStateName());
        elapsed = new QuestionState();
        elapsed.setState(DEADLINE_ELAPSED.getStateName());

        when(stateService.retrieveQuestionStateByStateName(ISSUE_PENDING.getStateName())).thenReturn(java.util.Optional.of(pending));
        when(stateService.retrieveQuestionStateByStateName(DEADLINE_ELAPSED.getStateName())).thenReturn(java.util.Optional.of(elapsed));

        onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(UUID.randomUUID());
        question = new Question();
        question.setOnlineHearing(onlineHearing);
        question.setQuestionState(pending);

        when(questionService.retrieveQuestionsDeadlineExpiredAndQuestionState(any(Date.class), any(QuestionState.class))).thenReturn(Arrays.asList(question));
        when(questionService.updateQuestionForced(question)).thenReturn(question);
        when(onlineHearingService.retrieveOnlineHearing(onlineHearing)).thenReturn(java.util.Optional.ofNullable(onlineHearing));
    }

    @Test
    public void testNoPendingState() {
        when(stateService.retrieveQuestionStateByStateName(ISSUE_PENDING.getStateName())).thenReturn(java.util.Optional.empty());
        trigger.execute();
        assertEquals(pending, question.getQuestionState());
    }

    @Test
    public void testNoElapsedState() {
        when(stateService.retrieveQuestionStateByStateName(DEADLINE_ELAPSED.getStateName())).thenReturn(java.util.Optional.empty());
        trigger.execute();
        assertEquals(pending, question.getQuestionState());
    }

    @Test
    public void testQuestionStateUpdatedAndUpdateQuestionInvoked() {
        trigger.execute();
        verify(questionService, times(1)).updateQuestionForced(question);
        assertEquals(elapsed, question.getQuestionState());
    }

    @Test
    public void testSessionEventCreateIsInvoked() {
        trigger.execute();
        verify(sessionEventService, times(1)).createSessionEvent(onlineHearing, EventTypes.QUESTION_DEADLINE_ELAPSED.getEventType());
    }
}
