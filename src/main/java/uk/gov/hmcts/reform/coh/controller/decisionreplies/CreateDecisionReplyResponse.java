package uk.gov.hmcts.reform.coh.controller.decisionreplies;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class CreateDecisionReplyResponse {
    @JsonProperty(value = "decision_reply_id")
    private UUID decisionReplyId;

    public CreateDecisionReplyResponse() {
        // For hibernate
    }

    public CreateDecisionReplyResponse(UUID decisionReplyId) {
        this.decisionReplyId = decisionReplyId;
    }

    public UUID getDecisionId() {
        return decisionReplyId;
    }

    public void setDecisionId(UUID decisionId) {
        this.decisionReplyId = decisionReplyId;
    }
}
