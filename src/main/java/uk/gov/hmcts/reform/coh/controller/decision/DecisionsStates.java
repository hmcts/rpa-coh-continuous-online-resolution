package uk.gov.hmcts.reform.coh.controller.decision;

public enum DecisionsStates {

    DECISION_DRAFTED("continuous_online_hearing_started"),
    DECISION_ISSUED("continuous_online_hearing_questions_issued"),
    DECISIONS_REJECTED("decision_rejected"),
    DECISIONS_ACCEPTED("decision_accepted");

    private String name;

    DecisionsStates(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
