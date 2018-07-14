package uk.gov.hmcts.reform.coh.controller.validators;

import java.util.function.Predicate;

public interface Validator<T> {

    Predicate<T> getPredicate();

    String getMessage();
}
