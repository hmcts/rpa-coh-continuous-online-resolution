package uk.gov.hmcts.reform.coh.controller.validators;

import uk.gov.hmcts.reform.coh.controller.decision.DecisionsStates;

import java.util.HashSet;
import java.util.Set;

public class DecisionRequestStateValidator {

    private static final Set<String> validStates;

    static {
        validStates = new HashSet<>();
        validStates.add(DecisionsStates.DECISION_DRAFTED.getStateName());
        validStates.add(DecisionsStates.DECISION_ISSUE_PENDING.getStateName());
    }

    public static boolean isValid(String decisionState) {
        return validStates.contains(decisionState);
    }
}
