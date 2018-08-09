package uk.gov.hmcts.reform.coh.controller.decisionreplies;

import uk.gov.hmcts.reform.coh.domain.DecisionReply;
import uk.gov.hmcts.reform.coh.states.DecisionsStates;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum DecisionReplyResponseMapper {

    DECISION_REPLY_ID(dr -> dr.getId().toString(), DecisionReplyResponse::setDecisionReplyId),
    DECISION_ID(dr -> dr.getDecision().getDecisionId().toString(), DecisionReplyResponse::setDecisionId),
    DECISION_REPLY_REASON(dr -> dr.getDecisionReplyReason(), DecisionReplyResponse::setDecisionReplyReason),
    AUTHOR_REFERENCE(dr -> dr.getAuthorReferenceId(), DecisionReplyResponse::setAuthorReference);

    private Function<DecisionReply, String> decisionReply;
    private BiConsumer<DecisionReplyResponse, String> setter;

    DecisionReplyResponseMapper(Function<DecisionReply, String> decisionReply, BiConsumer<DecisionReplyResponse, String> setter) {
        this.decisionReply = decisionReply;
        this.setter = setter;
    }

    public static void map(DecisionReply decisionReply, DecisionReplyResponse decisionReplyResponse) {
        for(DecisionReplyResponseMapper m : DecisionReplyResponseMapper.class.getEnumConstants()) {
            m.set(decisionReply, decisionReplyResponse);
        }

        String replyState = decisionReply.getDecisionReply() ? DecisionsStates.DECISIONS_ACCEPTED.getStateName() : DecisionsStates.DECISIONS_REJECTED.getStateName();
        decisionReplyResponse.setDecisionReply(replyState);
    }

    public void set(DecisionReply decisionReply, DecisionReplyResponse decisionReplyResponse) {
        setter.accept(decisionReplyResponse, this.decisionReply.apply(decisionReply));
    }
}
