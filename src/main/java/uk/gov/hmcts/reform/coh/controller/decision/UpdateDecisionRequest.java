package uk.gov.hmcts.reform.coh.controller.decision;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

public class UpdateDecisionRequest extends DecisionRequest {

    @ApiModelProperty(required = true, allowableValues = "decision_issue_pending, decision_drafted")
    @JsonProperty(value = "decision_state")
    private String state;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}

