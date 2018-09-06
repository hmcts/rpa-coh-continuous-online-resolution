package uk.gov.hmcts.reform.coh.controller.validators;

public interface BiValidator<T, U> {

    boolean test(T t, U u);

    String getMessage();
}
