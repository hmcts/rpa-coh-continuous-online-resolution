package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.controller.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.repository.SessionEventTypeRespository;

import java.util.Optional;

@Service
@Component
public class EventTypeService {

    private SessionEventTypeRespository sessionEventTypeRespository;

    @Autowired
    public EventTypeService(SessionEventTypeRespository sessionEventTypeRespository){
        this.sessionEventTypeRespository = sessionEventTypeRespository;
    }

    public Optional<SessionEventType> retrieveEventType(String eventType){
        Optional<SessionEventType> event = sessionEventTypeRespository.findByEventTypeName(eventType);

        if (!event.isPresent()){
            throw new ResourceNotFoundException("EventType Not Found");
        }

        return event;

    }

}
