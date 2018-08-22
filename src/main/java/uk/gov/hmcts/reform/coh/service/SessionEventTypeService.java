package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.repository.SessionEventTypeRespository;

import java.util.Optional;

@Service
public class SessionEventTypeService {

    private SessionEventTypeRespository sessionEventTypeRespository;

    @Autowired
    public SessionEventTypeService(SessionEventTypeRespository sessionEventTypeRespository){
        this.sessionEventTypeRespository = sessionEventTypeRespository;
    }

    public Optional<SessionEventType> retrieveEventType(String eventType){
        return sessionEventTypeRespository.findByEventTypeName(eventType);
    }

}
