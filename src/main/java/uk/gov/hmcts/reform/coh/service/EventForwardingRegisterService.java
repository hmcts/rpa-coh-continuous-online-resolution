package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingRegisterRepository;

import java.util.Optional;

@Service
public class EventForwardingRegisterService {

    private SessionEventForwardingRegisterRepository sessionEventForwardingRegisterRepository;

    @Autowired
    public EventForwardingRegisterService(SessionEventForwardingRegisterRepository sessionEventForwardingRegisterRepository) {
        this.sessionEventForwardingRegisterRepository = sessionEventForwardingRegisterRepository;
    }

    public SessionEventForwardingRegister createEventForwardingRegister(final SessionEventForwardingRegister sessionEventForwardingRegister) {
        return sessionEventForwardingRegisterRepository.save(sessionEventForwardingRegister);
    }

    public Optional<SessionEventForwardingRegister> retrieveEventForwardingRegister(final SessionEventForwardingRegister sessionEventForwardingRegister) {
        return sessionEventForwardingRegisterRepository.findById(sessionEventForwardingRegister.getEventForwardingRegisterId());
    }

    public void deleteEventForwardingRegister(final SessionEventForwardingRegister sessionEventForwardingRegister) {
        sessionEventForwardingRegisterRepository.delete(sessionEventForwardingRegister);
    }
}
