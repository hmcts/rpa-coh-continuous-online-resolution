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
import uk.gov.hmcts.reform.coh.states.QuestionStates;
import uk.gov.hmcts.reform.coh.util.QuestionEntityUtils;
import uk.gov.hmcts.reform.coh.util.QuestionStateUtils;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.coh.states.QuestionStates.*;

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

    private QuestionState issued;

    private QuestionState elapsed;

    private QuestionState granted;

    @Before
    public void setUp() {
        issued = QuestionStateUtils.get(ISSUED);
        elapsed = QuestionStateUtils.get(DEADLINE_ELAPSED);
        granted = QuestionStateUtils.get(QUESTION_DEADLINE_EXTENSION_GRANTED);

        when(stateService.retrieveQuestionStateByStateName(ISSUED.getStateName())).thenReturn(java.util.Optional.of(issued));
        when(stateService.retrieveQuestionStateByStateName(DEADLINE_ELAPSED.getStateName())).thenReturn(java.util.Optional.of(elapsed));
        when(stateService.retrieveQuestionStateByStateName(QUESTION_DEADLINE_EXTENSION_GRANTED.getStateName())).thenReturn(java.util.Optional.of(granted));

        onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(UUID.randomUUID());
        question = QuestionEntityUtils.createTestQuestion(QuestionStates.ISSUED, onlineHearing);

        when(questionService.retrieveQuestionsDeadlineExpiredAndQuestionStates(any(Date.class), anyList())).thenReturn(Arrays.asList(question));
        when(questionService.updateQuestionForced(question)).thenReturn(question);
        when(onlineHearingService.retrieveOnlineHearing(onlineHearing)).thenReturn(java.util.Optional.ofNullable(onlineHearing));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testNoPendingState() {
        when(stateService.retrieveQuestionStateByStateName(ISSUED.getStateName())).thenReturn(java.util.Optional.empty());
        trigger.execute();
        assertEquals(issued, question.getQuestionState());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testNoElapsedState() {
        when(stateService.retrieveQuestionStateByStateName(DEADLINE_ELAPSED.getStateName())).thenReturn(java.util.Optional.empty());
        trigger.execute();
        assertEquals(issued, question.getQuestionState());
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
