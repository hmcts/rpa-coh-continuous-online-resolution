package uk.gov.hmcts.reform.coh.controller.validators;

public interface Validator<T> {

    boolean test(T t);

    String getMessage();
}
