package uk.gov.hmcts.reform.coh.task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.states.DecisionsStates;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.DecisionService;
import uk.gov.hmcts.reform.coh.service.DecisionStateService;
import uk.gov.hmcts.reform.coh.states.OnlineHearingStates;
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

    @Mock
    private DecisionService decisionService;

    @Mock
    private DecisionStateService decisionStateService;

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
        given(decisionService.findByOnlineHearingId(onlineHearing.getOnlineHearingId())).willReturn(Optional.of(decision));
        given(decisionStateService.retrieveDecisionStateByState(DecisionsStates.DECISION_ISSUED.getStateName())).willReturn(Optional.ofNullable(decisionIssuedState));
    }

    @Test
    public void testDecisionNotFound() {
        onlineHearing.setOnlineHearingState(ohDecisionIssuedState);
        given(decisionStateService.retrieveDecisionStateByState(DecisionsStates.DECISION_ISSUED.getStateName())).willReturn(Optional.empty());
        decisionIssuedTask.execute(onlineHearing);
        assertEquals(decisionIssuedState, decision.getDecisionstate());
    }

    @Test
    public void testOnlineHearingAlreadyDecisionIssuedState() {
        onlineHearing.setOnlineHearingState(ohDecisionIssuedState);
        decisionIssuedTask.execute(onlineHearing);
        verify(onlineHearingService, times(0)).updateOnlineHearing(any(OnlineHearing.class));
    }

    @Test
    public void testOnlineHearingDecisionIssuedStateNotFound() {
        given(onlineHearingStateService.retrieveOnlineHearingStateByState(anyString())).willReturn(Optional.empty());
        decisionIssuedTask.execute(onlineHearing);
        verify(onlineHearingService, times(0)).updateOnlineHearing(any(OnlineHearing.class));
    }

    @Test
    public void testDecisionNotInIssuedState() {
        decision.setDecisionstate(decisionDraftedState);
        decisionIssuedTask.execute(onlineHearing);
        verify(onlineHearingService, times(0)).updateOnlineHearing(any(OnlineHearing.class));
    }

    @Test
    public void testOnlineHearingUpdatedToDecisionIssuedState() {
        given(onlineHearingService.retrieveOnlineHearing((onlineHearing))).willReturn(Optional.of(onlineHearing));
        decisionIssuedTask.execute(onlineHearing);
        assertEquals(ohDecisionIssuedState.getState(), onlineHearing.getOnlineHearingState().getState());
        verify(onlineHearingService, times(1)).updateOnlineHearing(onlineHearing);
    }

    @Test
    public void testDecisionIssuedTaskSupports() {
        assertEquals(1, decisionIssuedTask.supports().size());
        assertEquals(EventTypes.DECISION_ISSUED.getEventType(), decisionIssuedTask.supports().get(0));
    }
}
