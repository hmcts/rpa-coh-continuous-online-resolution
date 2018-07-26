package uk.gov.hmcts.reform.coh.states;

public enum QuestionStates {

    DRAFTED("question_drafted"),
    ISSUED_PENDING("question_issued_pending"),
    ISSUED("question_issued"),
    DEADLINE_ELAPSED("question_deadline_elapsed");

    private String stateName;

    QuestionStates(String stateName) {
        this.stateName = stateName;
    }

    public String getStateName() {
        return stateName;
    }
}
