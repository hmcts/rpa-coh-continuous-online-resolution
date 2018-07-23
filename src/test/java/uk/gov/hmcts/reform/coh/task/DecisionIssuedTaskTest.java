package uk.gov.hmcts.reform.coh.task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionsStates;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingStates;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionState;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class DecisionIssuedTaskTest {

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private OnlineHearingStateService onlineHearingStateService;

    @InjectMocks
    private DecisionIssuedTask decisionIssuedTask;

    private OnlineHearing onlineHearing;

    private OnlineHearingState ohDecisionIssuedState;

    private OnlineHearingState ohStartedIssuedState;

    private Decision decision;

    private DecisionState decisionDraftedState;

    private DecisionState decisionIssuedState;

    @Before
    public void setup() {
        ohDecisionIssuedState = new OnlineHearingState();
        ohDecisionIssuedState.setState(OnlineHearingStates.DECISION_ISSUED.getStateName());

        ohStartedIssuedState = new OnlineHearingState();
        ohStartedIssuedState.setState(OnlineHearingStates.STARTED.getStateName());

        decisionDraftedState = new DecisionState();
        decisionDraftedState.setState(DecisionsStates.DECISION_DRAFTED.getStateName());

        decisionIssuedState = new DecisionState();
        decisionIssuedState.setState(DecisionsStates.DECISION_ISSUED.getStateName());

        onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingState(ohStartedIssuedState);

        decision = new Decision();
        decision.setDecisionstate(decisionIssuedState);
        decision.setOnlineHearing(onlineHearing);

        given(onlineHearingStateService.retrieveOnlineHearingStateByState(anyString())).willReturn(Optional.of(ohDecisionIssuedState));
    }

    @Test
    public void testOnlineHearingAlreadyDecisionIssuedState() {
        onlineHearing.setOnlineHearingState(ohDecisionIssuedState);
        decisionIssuedTask.execute(decision);
        verify(onlineHearingService, times(0)).updateOnlineHearing(any(OnlineHearing.class));
    }

    @Test
    public void testOnlineHearingDecisionIssuedStateNotFound() {
        given(onlineHearingStateService.retrieveOnlineHearingStateByState(anyString())).willReturn(Optional.empty());
        decisionIssuedTask.execute(decision);
        verify(onlineHearingService, times(0)).updateOnlineHearing(any(OnlineHearing.class));
    }

    @Test
    public void testDecisionNotInIssuedState() {
        decision.setDecisionstate(decisionDraftedState);
        decisionIssuedTask.execute(decision);
        verify(onlineHearingService, times(0)).updateOnlineHearing(any(OnlineHearing.class));
    }

    @Test
    public void testOnlineHearingUpdatedToDecisionIssuedState() {
        decisionIssuedTask.execute(decision);
        assertEquals(ohDecisionIssuedState.getState(), onlineHearing.getOnlineHearingState().getState());
        verify(onlineHearingService, times(1)).updateOnlineHearing(onlineHearing);
    }
}
