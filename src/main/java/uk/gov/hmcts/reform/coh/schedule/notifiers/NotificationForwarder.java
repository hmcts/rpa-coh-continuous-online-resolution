package uk.gov.hmcts.reform.coh.schedule.notifiers;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;

public interface NotificationForwarder<T> {

    ResponseEntity sendEndpoint(SessionEventForwardingRegister register, T t) throws NotificationException;
}
