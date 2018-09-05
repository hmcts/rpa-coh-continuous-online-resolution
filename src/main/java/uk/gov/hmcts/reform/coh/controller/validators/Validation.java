package uk.gov.hmcts.reform.coh.controller.validators;

public class Validation<T, U> {

    public ValidationResult execute(Validator [] validators, T t) {

        ValidationResult result = new ValidationResult();
        result.setValid(true);

        for (Validator validator : validators) {
            if (!validator.test(t)) {
                result.setValid(false);
                result.setReason(validator.getMessage());
                return result;
            }
        }

        return result;
    }

    public ValidationResult execute(BiValidator [] validators, T t, U u) {

        ValidationResult result = new ValidationResult();
        result.setValid(true);

        for (BiValidator validator : validators) {
            if (!validator.test(t, u)) {
                result.setValid(false);
                result.setReason(validator.getMessage());
                return result;
            }
        }

        return result;
    }
}
