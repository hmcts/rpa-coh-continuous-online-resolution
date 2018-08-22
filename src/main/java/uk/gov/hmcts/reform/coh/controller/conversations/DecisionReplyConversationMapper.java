package uk.gov.hmcts.reform.coh.controller.conversations;

import uk.gov.hmcts.reform.coh.controller.decisionreplies.DecisionReplyResponse;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.DecisionReplyResponseMapper;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionReply;

public class DecisionReplyConversationMapper {

    private DecisionReplyConversationMapper() {}

    public static void map(DecisionReply decisionReply, DecisionReplyResponse response) {
        DecisionReplyResponseMapper.map(decisionReply, response);

        Decision decision = decisionReply.getDecision();
        response.setUri(CohUriBuilder
            .buildDecisionReplyGet(decision.getOnlineHearing().getOnlineHearingId(), decisionReply.getId()));
    }
}
