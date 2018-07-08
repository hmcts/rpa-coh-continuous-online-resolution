package uk.gov.hmcts.reform.coh.controller.decision;

import uk.gov.hmcts.reform.coh.domain.Decision;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum  DecisionRequestMapper {

    DECISION_HEADER(DecisionRequest::getDecisionHeader, Decision::setDecisionHeader),
    DECISION_TEXT(DecisionRequest::getDecisionText, Decision::setDecisionText),
    DECISION_REASON(DecisionRequest::getDecisionReason, Decision::setDecisionReason),
    DECISION_AWARD(DecisionRequest::getDecisionAward, Decision::setDecisionAward);

    private Function<DecisionRequest, String> getter;

    private BiConsumer<Decision, String> setter;

    DecisionRequestMapper(Function<DecisionRequest, String> getter, BiConsumer<Decision, String> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public static void map(DecisionRequest request, Decision decision) {
        for (DecisionRequestMapper m : DecisionRequestMapper.class.getEnumConstants()) {
            m.set(request, decision);
        }
    }

    public void set(DecisionRequest request, Decision decision) {
        setter.accept(decision, getter.apply(request));
    }
}
