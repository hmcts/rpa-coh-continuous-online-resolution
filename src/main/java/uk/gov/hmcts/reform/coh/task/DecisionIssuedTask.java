package uk.gov.hmcts.reform.coh.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionResponse;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionsStates;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingStates;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class DecisionIssuedTask implements ContinuousOnlineResolutionTask<Decision> {

    private static final Logger log = LoggerFactory.getLogger(DecisionIssuedTask.class);

    private OnlineHearingService onlineHearingService;

    private OnlineHearingStateService onlineHearingStateService;

    private String ohDecisionIssuedState;

    private String decisionIssuedState;

    @Autowired
    public DecisionIssuedTask(OnlineHearingService onlineHearingService, OnlineHearingStateService onlineHearingStateService) {
        this.onlineHearingService = onlineHearingService;
        this.onlineHearingStateService = onlineHearingStateService;

        ohDecisionIssuedState = OnlineHearingStates.DECISION_ISSUED.getStateName();
        decisionIssuedState = DecisionsStates.DECISION_ISSUED.getStateName();
    }

    @Override
    public void execute(Decision decision) {

        OnlineHearing onlineHearing = decision.getOnlineHearing();
        if (ohDecisionIssuedState.equals(onlineHearing.getOnlineHearingState().getState())) {
            // Already in required state
            return;
        }

        Optional<OnlineHearingState> state = onlineHearingStateService.retrieveOnlineHearingStateByState(ohDecisionIssuedState);
        if (!state.isPresent()) {
            log.debug("Unable to find required online hearing state: " + ohDecisionIssuedState);
            return;
        }

        if (decisionIssuedState.equals(decision.getDecisionstate().getState())) {
            onlineHearing.setOnlineHearingState(state.get());
            onlineHearing.registerStateChange();
            onlineHearingService.updateOnlineHearing(onlineHearing);
        }
    }

    @Override
    public List<String> getName() {
        return Arrays.asList(EventTypes.DECISION_ISSUED.getEventType());
    }
}
