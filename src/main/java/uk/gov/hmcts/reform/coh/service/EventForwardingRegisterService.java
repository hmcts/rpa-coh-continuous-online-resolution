package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.EventForwardingRegister;
import uk.gov.hmcts.reform.coh.repository.EventForwardingRegisterRepository;

import java.util.Optional;

@Service
@Component
public class EventForwardingRegisterService {

    private EventForwardingRegisterRepository eventForwardingRegisterRepository;

    @Autowired
    public EventForwardingRegisterService(EventForwardingRegisterRepository eventForwardingRegisterRepository) {
        this.eventForwardingRegisterRepository = eventForwardingRegisterRepository;
    }

    public EventForwardingRegister createEventForwardingRegister(final EventForwardingRegister eventForwardingRegister) {
        return eventForwardingRegisterRepository.save(eventForwardingRegister);
    }

    public Optional<EventForwardingRegister> retrieveEventForwardingRegister(final EventForwardingRegister eventForwardingRegister) {
        return eventForwardingRegisterRepository.findById(eventForwardingRegister.getEventForwardingRegisterId());
    }

    public void deleteEventForwardingRegister(final EventForwardingRegister eventForwardingRegister) {
        eventForwardingRegisterRepository.delete(eventForwardingRegister);
    }
}
