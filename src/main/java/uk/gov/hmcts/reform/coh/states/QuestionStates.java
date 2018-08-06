package uk.gov.hmcts.reform.coh.states;

public enum QuestionStates {

    DRAFTED("question_drafted"),
    ISSUE_PENDING("question_issue_pending"),
    ISSUED("question_issued"),
    DEADLINE_ELAPSED("question_deadline_elapsed"),
    QUESTION_EXTENSION_REQUESTED("question_extension_requested"),
    QUESTION_DEADLINE_EXTENSION_DENIED("question_deadline_extension_denied"),
    QUESTION_DEADLINE_EXTENSION_GRANTED("question_deadline_extension_granted");

    private String stateName;

    QuestionStates(String stateName) {
        this.stateName = stateName;
    }

    public String getStateName() {
        return stateName;
    }
}
