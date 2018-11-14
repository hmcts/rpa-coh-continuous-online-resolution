package uk.gov.hmcts.reform.coh.exception;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class GenericException extends UnknownErrorCodeException {

    public GenericException(AlertLevel level, Throwable e) {
        super(level, e);
    }
}
