package uk.gov.hmcts.reform.coh.controller.decisionreplies;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class CreateDecisionReplyResponse {
    @JsonProperty(value = "decision_reply_id")
    private UUID decisionId;

    public CreateDecisionReplyResponse() {
        // For hibernate
    }

    public CreateDecisionReplyResponse(UUID decisionId) {
        this.decisionId = decisionId;
    }

    public UUID getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(UUID decisionId) {
        this.decisionId = decisionId;
    }
}
