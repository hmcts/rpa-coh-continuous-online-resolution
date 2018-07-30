package uk.gov.hmcts.reform.coh.task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;
import uk.gov.hmcts.reform.coh.service.QuestionRoundService;
import uk.gov.hmcts.reform.coh.service.QuestionStateService;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.coh.events.EventTypes.QUESTION_ROUND_ISSUED;
import static uk.gov.hmcts.reform.coh.states.OnlineHearingStates.QUESTIONS_ISSUED;
import static uk.gov.hmcts.reform.coh.states.OnlineHearingStates.STARTED;

@RunWith(SpringRunner.class)
public class QuestionRoundSentTaskTest {

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private OnlineHearingStateService onlineHearingStateService;

    @Mock
    private QuestionRoundService questionRoundService;

    @Mock
    private QuestionStateService questionStateService;

    @InjectMocks
    private QuestionRoundSentTask questionRoundSentTask;

    private OnlineHearing onlineHearing;
    private OnlineHearingState startedState;
    private OnlineHearingState issuedState;

    private OnlineHearing returnedOh;

    @Before
    public void setup() {
        startedState = new OnlineHearingState();
        startedState.setState(STARTED.getStateName());

        issuedState = new OnlineHearingState();
        issuedState.setState(QUESTIONS_ISSUED.getStateName());

        onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingState(startedState);

        returnedOh = new OnlineHearing();
        given(onlineHearingService.updateOnlineHearing(onlineHearing)).willReturn(returnedOh);
        given(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.ofNullable(returnedOh));
        given(onlineHearingStateService.retrieveOnlineHearingStateByState(QUESTIONS_ISSUED.getStateName())).willReturn(Optional.of(issuedState));


        QuestionState issuedPending = new QuestionState();
        issuedPending.setState(QuestionStates.ISSUE_PENDING.getStateName());
        issuedPending.setQuestionStateId(20);
        given(questionStateService.retrieveQuestionStateByStateName(anyString())).willReturn(Optional.of(issuedPending));

        List<Question> questionRound = new ArrayList<>();
        Question question = new Question();
        question.setQuestionState(issuedPending);
        question.setQuestionRound(1);
        questionRound.add(question);
        given(questionRoundService.getQuestionsByQuestionRound(any(OnlineHearing.class), anyInt())).willReturn(questionRound);
        given(questionRoundService.issueQuestionRound(any(QuestionState.class), anyList())).willReturn(questionRound);

    }

    @Test
    public void testSave() {
        questionRoundSentTask.execute(onlineHearing);
        verify(questionRoundService, times(1)).issueQuestionRound(any(QuestionState.class),
                anyList());

    }

    @Test
    public void testSupports() {
        List<String> supports = questionRoundSentTask.supports();
        assertTrue(supports.contains(QUESTION_ROUND_ISSUED.getEventType()));
    }

    @Test
    public void testQuestionIssuedStateNotFound() {
        given(onlineHearingStateService.retrieveOnlineHearingStateByState(QuestionStates.ISSUED.getStateName())).willReturn(Optional.empty());
        questionRoundSentTask.execute(onlineHearing);
        assertEquals(startedState, onlineHearing.getOnlineHearingState());
    }

    @Test
    public void testOnlineHearingStateQuestionIssuedStateNotFound() {
        given(onlineHearingStateService.retrieveOnlineHearingStateByState(QUESTIONS_ISSUED.getStateName())).willReturn(Optional.empty());
        questionRoundSentTask.execute(onlineHearing);
        assertEquals(startedState, onlineHearing.getOnlineHearingState());
    }
}
