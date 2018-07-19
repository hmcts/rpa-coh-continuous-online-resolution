package uk.gov.hmcts.reform.coh.controller.validators;

import org.apache.commons.validator.routines.IntegerValidator;

public class ValidatorUtils {

    public final static boolean isPositiveInteger(String candidate) {

        return IntegerValidator.getInstance().isValid(candidate)
                    && IntegerValidator.getInstance().minValue(Integer.parseInt(candidate), 0);
    }
}
