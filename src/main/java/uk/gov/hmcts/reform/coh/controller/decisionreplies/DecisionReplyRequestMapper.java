package uk.gov.hmcts.reform.coh.controller.decisionreplies;

import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionReply;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum DecisionReplyRequestMapper {
    DECISION_HEADER(DecisionReplyRequest::getDecisionReply, DecisionReply::setDecisionReply),
    DECISION_TEXT(DecisionReplyRequest::getDecisionReplyReason, DecisionReply::setDecisionReplyReason),
    DECISION_REASON(DecisionReplyRequest::getAuthorReferenceId, DecisionReply::setAuthorReferenceId);

    private Function<DecisionReplyRequest, String> getter;

    private BiConsumer<DecisionReply, String> setter;

    DecisionReplyRequestMapper(Function<DecisionReplyRequest, String> getter, BiConsumer<DecisionReply, String> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public static void map(DecisionReplyRequest request, DecisionReply decisionReply, Decision decision) {
        for (DecisionReplyRequestMapper m : DecisionReplyRequestMapper.class.getEnumConstants()) {
            m.set(request, decisionReply);
        }
        decisionReply.setDecision(decision);
    }

    public void set(DecisionReplyRequest request, DecisionReply decision) {
        setter.accept(decision, getter.apply(request));
    }
}
