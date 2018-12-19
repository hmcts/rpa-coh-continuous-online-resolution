package uk.gov.hmcts.reform.coh.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class GenericException extends RuntimeException {

    public GenericException(Throwable e) {
        super(e);
    }
}
