package uk.gov.hmcts.reform.coh.controller.decision;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateDecisionRequest extends DecisionRequest {

    @JsonProperty(value = "decision_state")
    private String state;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
