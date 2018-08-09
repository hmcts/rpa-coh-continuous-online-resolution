package uk.gov.hmcts.reform.coh.controller.decision;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.DecisionReplyResponse;
import uk.gov.hmcts.reform.coh.controller.state.StateResponse;

import java.util.ArrayList;
import java.util.List;

public class DecisionResponse {

    @JsonProperty(value = "decision_id")
    private String decisionId;

    @JsonProperty(value = "online_hearing_id")
    private String onlineHearingId;

    @JsonProperty(value = "decision_header")
    private String decisionHeader;

    @JsonProperty(value = "decision_text")
    private String decisionText;

    @JsonProperty(value = "decision_reason")
    private String decisionReason;

    @JsonProperty(value = "decision_award")
    private String decisionAward;

    @JsonProperty(value = "deadline_expiry_date")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String deadlineExpiryDate;

    @JsonProperty(value = "history")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<StateResponse> histories = new ArrayList<>();

    @JsonProperty(value = "decision_state")
    private DecisionState decisionState = new DecisionState();

    @JsonProperty(value = "uri")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String uri;

    @JsonProperty(value = "decision_replies")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<DecisionReplyResponse> decisionReplyResponses;


    public String getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(String decisionId) {
        this.decisionId = decisionId;
    }

    public String getOnlineHearingId() {
        return onlineHearingId;
    }

    public void setOnlineHearingId(String onlineHearingId) {
        this.onlineHearingId = onlineHearingId;
    }

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

    public String getDeadlineExpiryDate() {
        return deadlineExpiryDate;
    }

    public void setDeadlineExpiryDate(String deadlineExpiryDate) {
        this.deadlineExpiryDate = deadlineExpiryDate;
    }

    public DecisionState getDecisionState() {
        return decisionState;
    }

    public void setDecisionState(DecisionState decisionState) {
        this.decisionState = decisionState;
    }

    public void setDecisionStateName(String stateName) {
        getDecisionState().setStateName(stateName);
    }

    public void setDecisionStateDatetime(String stateDatetime) {
        getDecisionState().setStateDatetime(stateDatetime);
    }

    public List<StateResponse> getHistories() {
        return histories;
    }

    public void setHistories(List<StateResponse> histories) {
        this.histories = histories;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setDecisionReplies(List<DecisionReplyResponse> decisionReplyResponses) {
        this.decisionReplyResponses = decisionReplyResponses;
    }

    public List<DecisionReplyResponse> getDecisionReplyResponses() {
        return decisionReplyResponses;
    }

    public static class DecisionState {

        @JsonProperty(value = "state_name")
        private String stateName;

        @JsonProperty(value = "state_datetime")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String stateDatetime;

        public String getStateName() {
            return stateName;
        }

        public void setStateName(String stateName) {
            this.stateName = stateName;
        }

        public String getStateDatetime() {
            return stateDatetime;
        }

        public void setStateDatetime(String stateDatetime) {
            this.stateDatetime = stateDatetime;
        }
    }
}
