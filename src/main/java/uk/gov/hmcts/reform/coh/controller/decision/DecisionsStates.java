package uk.gov.hmcts.reform.coh.controller.decision;

public enum DecisionsStates {

    DECISION_DRAFTED("decision_drafted"),
    DECISION_ISSUED("decision_issued"),
    DECISION_ISSUE_PENDING("decision_issue_pending"),
    DECISIONS_REJECTED("decision_rejected"),
    DECISIONS_ACCEPTED("decision_accepted");

    private String stateName;

    DecisionsStates(String stateName) {
        this.stateName = stateName;
    }

    public String getStateName() {
        return stateName;
    }
}
