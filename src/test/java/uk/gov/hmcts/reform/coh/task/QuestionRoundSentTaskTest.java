package uk.gov.hmcts.reform.coh.task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.coh.events.EventTypes.QUESTION_ROUND_ISSUED;
import static uk.gov.hmcts.reform.coh.states.OnlineHearingStates.QUESTIONS_ISSUED;
import static uk.gov.hmcts.reform.coh.states.OnlineHearingStates.STARTED;

@RunWith(SpringRunner.class)
public class QuestionRoundSentTaskTest {

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private OnlineHearingStateService onlineHearingStateService;

    @InjectMocks
    private QuestionRoundSentTask questionRoundSentTask;

    private OnlineHearing onlineHearing;

    private OnlineHearingState startedState;

    private OnlineHearingState issuedState;

    @Before
    public void setup() {
        startedState = new OnlineHearingState();
        startedState.setState(STARTED.getStateName());

        issuedState = new OnlineHearingState();
        issuedState.setState(QUESTIONS_ISSUED.getStateName());

        onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingState(startedState);

        given(onlineHearingService.updateOnlineHearing(onlineHearing)).willReturn(onlineHearing);
        given(onlineHearingStateService.retrieveOnlineHearingStateByState(QUESTIONS_ISSUED.getStateName())).willReturn(java.util.Optional.ofNullable(issuedState));
    }

    @Test
    public void testSave() {
        questionRoundSentTask.execute(onlineHearing);
        assertEquals(issuedState, onlineHearing.getOnlineHearingState());
    }

    @Test
    public void testSupports() {
        List<String> supports = questionRoundSentTask.supports();
        assertTrue(supports.contains(QUESTION_ROUND_ISSUED.getEventType()));
    }

    @Test
    public void testQuestionIssuedStateNotFound() {
        given(onlineHearingStateService.retrieveOnlineHearingStateByState(QUESTIONS_ISSUED.getStateName())).willReturn(java.util.Optional.empty());
        questionRoundSentTask.execute(onlineHearing);
        assertEquals(startedState, onlineHearing.getOnlineHearingState());
    }
}
