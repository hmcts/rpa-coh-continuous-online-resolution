package uk.gov.hmcts.reform.coh.controller.validators;

import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionRequest;

import java.util.function.Predicate;

public enum DecisionRequestValidator implements Validator<DecisionRequest>{
    DECISION_HEADER( r -> StringUtils.isEmpty(r.getDecisionHeader()), "Decision header is required"),
    DECISION_TEXT( r -> StringUtils.isEmpty(r.getDecisionText()), "Decision text is required"),
    DECISION_REASON( r -> StringUtils.isEmpty(r.getDecisionReason()), "Decision reason is required"),
    DECISION_AWARD( r -> StringUtils.isEmpty(r.getDecisionAward()), "Decision award is required");

    private Predicate<DecisionRequest> predicate;
    private String message;

    DecisionRequestValidator(Predicate<DecisionRequest> validator, String message) {
        this.predicate = validator;
        this.message = message;
    }

    public Predicate<DecisionRequest> getPredicate() {
        return predicate;
    }

    public String getMessage() {
        return message;
    }
}
