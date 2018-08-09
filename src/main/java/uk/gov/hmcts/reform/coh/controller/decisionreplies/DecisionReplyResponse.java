package uk.gov.hmcts.reform.coh.controller.decisionreplies;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DecisionReplyResponse {

    @JsonProperty("decision_reply_id")
    private String decisionReplyId;

    @JsonProperty("decision_id")
    private String decisionId;

    @JsonProperty("decision_reply")
    private String decisionReply;

    @JsonProperty("decision_reply_reason")
    private String decisionReplyReason;

    @JsonProperty("author_reference")
    private String authorReference;

    @JsonProperty(value = "decision_reply_date")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String decisionReplyDate;

    @JsonProperty(value = "uri")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String uri;

    public String getDecisionReplyDate() {
        return decisionReplyDate;
    }

    public void setDecisionReplyDate(String decisionReplyDate) {
        this.decisionReplyDate = decisionReplyDate;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDecisionReplyId() {
        return decisionReplyId;
    }

    public void setDecisionReplyId(String decisionReplyId) {
        this.decisionReplyId = decisionReplyId;
    }

    public String getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(String decisionId) {
        this.decisionId = decisionId;
    }

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

    public String getAuthorReference() {
        return authorReference;
    }

    public void setAuthorReference(String authorReference) {
        this.authorReference = authorReference;
    }
}
