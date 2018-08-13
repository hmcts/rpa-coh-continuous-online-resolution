package uk.gov.hmcts.reform.coh.controller.decision;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

public class DecisionRequest {

    @JsonProperty(value = "decision_header")
    @ApiModelProperty(required = true)
    private String decisionHeader;

    @JsonProperty(value = "decision_text")
    @ApiModelProperty(required = true)
    private String decisionText;

    @JsonProperty(value = "decision_reason")
    @ApiModelProperty(required = true)
    private String decisionReason;

    @JsonProperty(value = "decision_award")
    @ApiModelProperty(required = true)
    private String decisionAward;

    public String getDecisionHeader() {
        return decisionHeader;
    }

    public void setDecisionHeader(String decisionHeader) {
        this.decisionHeader = decisionHeader;
    }

    public String getDecisionText() {
        return decisionText;
    }

    public void setDecisionText(String decisionText) {
        this.decisionText = decisionText;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }

    public String getDecisionAward() {
        return decisionAward;
    }

    public void setDecisionAward(String decisionAward) {
        this.decisionAward = decisionAward;
    }
}
