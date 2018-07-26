package uk.gov.hmcts.reform.coh.states;

public enum OnlineHearingStates {

    STARTED("continuous_online_hearing_started"),
    QUESTIONS_ISSUED("continuous_online_hearing_questions_issued"),
    ANSWERS_SENT("continuous_online_hearing_answers_sent"),
    DECISION_ISSUED("continuous_online_hearing_decision_issued"),
    RESOLVED("continuous_online_hearing_resolved"),
    RELISTED("continuous_online_hearing_relisted"),
    CLOSED("continuous_online_hearing_questions_closed");

    private String stateName;

    OnlineHearingStates(String stateName) { this.stateName = stateName; }

    public String getStateName() {
        return stateName;
    }

}
