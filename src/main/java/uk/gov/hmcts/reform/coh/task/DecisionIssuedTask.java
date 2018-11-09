package uk.gov.hmcts.reform.coh.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.states.DecisionsStates;
import uk.gov.hmcts.reform.coh.domain.DecisionState;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.DecisionService;
import uk.gov.hmcts.reform.coh.service.DecisionStateService;
import uk.gov.hmcts.reform.coh.states.OnlineHearingStates;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class DecisionIssuedTask implements ContinuousOnlineResolutionTask<OnlineHearing> {

    private static final Logger log = LoggerFactory.getLogger(DecisionIssuedTask.class);

    private OnlineHearingService onlineHearingService;

    private OnlineHearingStateService onlineHearingStateService;

    private DecisionService decisionService;

    private DecisionStateService decisionStateService;

    @Autowired
    public DecisionIssuedTask(OnlineHearingService onlineHearingService, OnlineHearingStateService onlineHearingStateService, DecisionService decisionService, DecisionStateService decisionStateService) {
        this.onlineHearingService = onlineHearingService;
        this.onlineHearingStateService = onlineHearingStateService;
        this.decisionService = decisionService;
        this.decisionStateService = decisionStateService;
    }

    @Override
    @Transactional
    public void execute(OnlineHearing onlineHearing) {
        log.info(String.format("Executing: %s", this.getClass()));

        Optional<Decision> optDecision = decisionService.findByOnlineHearingId(onlineHearing.getOnlineHearingId());
        if (!optDecision.isPresent()) {
            log.warn(String.format("Unable to find the decision for online hearing: %s", onlineHearing.getOnlineHearingId()));
            return;
        }

        // Update the decision pending state to issued
        Optional<DecisionState> optDecisionState = decisionStateService.retrieveDecisionStateByState(DecisionsStates.DECISION_ISSUED.getStateName());
        if (!optDecisionState.isPresent()) {
            log.warn(String.format("Unable to find decision state : %s", DecisionsStates.DECISION_ISSUED.getStateName()));
            return;
        }
        DecisionState decisionIssuedState = optDecisionState.get();
        log.info(String.format("Updating decision state to : %s", decisionIssuedState.getState()));
        Decision decision = optDecision.get();
        decision.addDecisionStateHistory(decisionIssuedState);
        decision.setDecisionstate(decisionIssuedState);
        decisionService.updateDecision(decision);

        // Update the online hearing state to issued
        Optional<OnlineHearingState> optOnlineHearingState = onlineHearingStateService.retrieveOnlineHearingStateByState(OnlineHearingStates.DECISION_ISSUED.getStateName());
        if (!optOnlineHearingState.isPresent()) {
            log.warn(String.format("Unable to find online hearing state : %s", OnlineHearingStates.DECISION_ISSUED.getStateName()));
            return;
        }

        OnlineHearingState ohDecisionIssuedState = optOnlineHearingState.get();
        log.info(String.format("Updating online hearing state to : %s", ohDecisionIssuedState.getState()));

        // Having to do this because JPA is a pain
        Optional<OnlineHearing> optOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearing);
        if (optOnlineHearing.isPresent()) {
            OnlineHearing newOnlineHearing = optOnlineHearing.get();
            newOnlineHearing.setOnlineHearingState(ohDecisionIssuedState);
            newOnlineHearing.registerStateChange();
            onlineHearingService.updateOnlineHearing(newOnlineHearing);
        }
    }

    @Override
    public List<String> supports() {
        return Arrays.asList(EventTypes.DECISION_ISSUED.getEventType());
    }
}
