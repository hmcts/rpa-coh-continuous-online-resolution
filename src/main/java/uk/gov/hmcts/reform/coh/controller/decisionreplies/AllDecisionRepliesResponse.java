package uk.gov.hmcts.reform.coh.controller.decisionreplies;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class AllDecisionRepliesResponse {

    @JsonProperty(value = "decision_replies")
    private List<DecisionReplyResponse> decisionReplyList = new ArrayList<>();

    public void addDecisionReply(DecisionReplyResponse replyResponse) {
        decisionReplyList.add(replyResponse);
    }

    public List<DecisionReplyResponse> getDecisionReplyList() {
        return decisionReplyList;
    }
}
