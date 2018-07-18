package uk.gov.hmcts.reform.coh.states;

public enum AnswerStates {

    DRAFTED("answer_drafted"),
    SUBMITTED("answer_submitted");

    private String stateName;

    AnswerStates(String stateName) {
        this.stateName = stateName;
    }

    public String getStateName() {
        return stateName;
    }
}
