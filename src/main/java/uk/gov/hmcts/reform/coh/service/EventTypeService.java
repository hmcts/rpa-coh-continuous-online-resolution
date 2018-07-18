package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.EventType;
import uk.gov.hmcts.reform.coh.repository.EventTypeRepository;

import java.util.Optional;

@Service
@Component
public class EventTypeService {

    private EventTypeRepository eventTypeRepository;

    @Autowired
    public EventTypeService(EventTypeRepository eventTypeRepository){
        this.eventTypeRepository = eventTypeRepository;
    }

    public Optional<EventType> retrieveEventType(String eventType){
        return eventTypeRepository.findByEventTypeName(eventType);
    }

}
