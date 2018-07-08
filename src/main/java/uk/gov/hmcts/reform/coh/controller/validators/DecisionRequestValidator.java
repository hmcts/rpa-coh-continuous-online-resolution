package uk.gov.hmcts.reform.coh.controller.validators;

import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionRequest;

import java.util.function.Predicate;

public enum DecisionRequestValidator {
    DECISION_HEADER( r -> StringUtils.isEmpty(r.getDecisionHeader()), "Decision header is required"),
    DECISION_TEXT( r -> StringUtils.isEmpty(r.getDecisionText()), "Decision text is required"),
    DECISION_REASON( r -> StringUtils.isEmpty(r.getDecisionReason()), "Decision reason is required"),
    DECISION_AWARD( r -> StringUtils.isEmpty(r.getDecisionAward()), "Decision award is required");

    private Predicate<DecisionRequest> tester;
    private String message;

    DecisionRequestValidator(Predicate<DecisionRequest> validator, String message) {
        this.tester = validator;
        this.message = message;
    }

    public static ValidationResult validate(DecisionRequest request) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        for (DecisionRequestValidator validator : DecisionRequestValidator.class.getEnumConstants()) {

            if (validator.tester.test(request)) {
                result.setReason(validator.message);
                result.setValid(false);
            }
        }

        return result;
    }
}
