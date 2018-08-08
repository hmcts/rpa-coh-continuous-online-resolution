package uk.gov.hmcts.reform.coh.controller.decisionreplies;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class CreateDecisionReplyResponse {

    @JsonProperty(value = "decision_reply_id")
    private UUID decisionReplyId;

    public CreateDecisionReplyResponse() {
        // Hibernate
    }

    public CreateDecisionReplyResponse(UUID decisionReplyId) {
        this.decisionReplyId = decisionReplyId;
    }

    public UUID getDecisionReplyId() {
        return decisionReplyId;
    }

    public void setDecisionReplyId(UUID decisionReplyId) {
        this.decisionReplyId = decisionReplyId;
    }
}
