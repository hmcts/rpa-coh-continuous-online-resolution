package uk.gov.hmcts.reform.coh.states;

public enum SessionEventForwardingStates {

    EVENT_FORWARDING_FAILED("event_forwarding_failed"),
    EVENT_FORWARDING_PENDING("event_forwarding_pending"),
    EVENT_FORWARDING_SUCCESS("event_forwarding_success");

    private String stateName;

    SessionEventForwardingStates(String stateName) {
        this.stateName = stateName;
    }

    public String getStateName() {
        return stateName;
    }
}
