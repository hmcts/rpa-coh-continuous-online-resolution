package uk.gov.hmcts.reform.coh.controller.validators;

import org.apache.commons.validator.routines.IntegerValidator;

public class ValidatorUtils {

    private ValidatorUtils() {}

    public static final boolean isPositiveInteger(String candidate) {

        return IntegerValidator.getInstance().isValid(candidate)
                    && IntegerValidator.getInstance().minValue(Integer.parseInt(candidate), 0);
    }
}
