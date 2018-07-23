package uk.gov.hmcts.reform.coh.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.UNPROCESSABLE_ENTITY)
public class NotificationException extends RuntimeException {
    public NotificationException(String exception) {
        super(exception);
    }
}
