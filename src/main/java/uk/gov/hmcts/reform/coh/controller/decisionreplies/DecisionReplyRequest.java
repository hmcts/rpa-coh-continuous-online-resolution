package uk.gov.hmcts.reform.coh.controller.decisionreplies;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class DecisionReplyRequest {

    @JsonProperty("decision_reply")
    @NotNull(message = "Decision reply required")
    @NotBlank(message = "Decision reply required")
    @ApiModelProperty(required = true)
    private String decisionReply;

    @JsonProperty("decision_reply_reason")
    @NotNull(message = "Decision reply reason required")
    @NotBlank(message = "Decision reply reason required")
    @ApiModelProperty(required = true)
    private String decisionReplyReason;


    public String getDecisionReply() {
        return decisionReply;
    }

    public void setDecisionReply(String decisionReply) {
        this.decisionReply = decisionReply;
    }

    public String getDecisionReplyReason() {
        return decisionReplyReason;
    }

    public void setDecisionReplyReason(String decisionReplyReason) {
        this.decisionReplyReason = decisionReplyReason;
    }
}
