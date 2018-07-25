package uk.gov.hmcts.reform.coh.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingRegisterRepository;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingStateRepository;
import uk.gov.hmcts.reform.coh.repository.SessionEventRepository;
import uk.gov.hmcts.reform.coh.repository.SessionEventTypeRespository;
import uk.gov.hmcts.reform.coh.states.SessionEventForwardingStates;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class SessionEventService {

    private static final Logger log = LoggerFactory.getLogger(SessionEventService.class);

    private static final String STARTING_STATE = SessionEventForwardingStates.EVENT_FORWARDING_PENDING.getStateName();

    @Autowired
    private SessionEventRepository sessionEventRepository;

    @Autowired
    private SessionEventTypeRespository sessionEventTypeRespository;

    @Autowired
    private SessionEventForwardingStateRepository sessionEventForwardingStateRepository;

    @Autowired
    private SessionEventForwardingRegisterRepository sessionEventForwardingRegisterRepository;

    public SessionEvent createSessionEvent(OnlineHearing onlineHearing, String sessionEventType) {

        Optional<SessionEventType> optSessionEventType = sessionEventTypeRespository.findByEventTypeName(sessionEventType);
        if (!optSessionEventType.isPresent()) {
            String message = "Session Event Type '" + sessionEventType + "' not found";
            log.error(message);
            throw new EntityNotFoundException(message);
        }

        return createSessionEvent(onlineHearing, optSessionEventType.get());
    }

    public SessionEvent createSessionEvent(OnlineHearing onlineHearing, SessionEventType sessionEventType) {

        Optional<SessionEventForwardingState> optForwardingState = sessionEventForwardingStateRepository.findByForwardingStateName(STARTING_STATE);
        if (!optForwardingState.isPresent()) {
            String message = "Session Event Forwarding State '" + STARTING_STATE + "' not found";
            log.error(message);
            throw new EntityNotFoundException(message);
        }

        Optional<SessionEventForwardingRegister> optRegister = sessionEventForwardingRegisterRepository.findByJurisdictionAndSessionEventType(onlineHearing.getJurisdiction(), sessionEventType);
        if (!optRegister.isPresent()) {
            String message = "Session event registry entry not found for jurisdiction '" + onlineHearing.getJurisdiction().getJurisdictionName() + "', session event type '" + sessionEventType.getEventTypeName() + "'";
            log.error(message);
            throw new EntityNotFoundException(message);
        }

        SessionEvent sessionEvent = new SessionEvent();
        sessionEvent.setOnlineHearing(onlineHearing);
        sessionEvent.setSessionEventForwardingRegister(optRegister.get());
        sessionEvent.setSessionEventForwardingState(optForwardingState.get());

        return sessionEventRepository.save(sessionEvent);
    }

    public Optional<SessionEvent> retrieveByOnlineHearing(OnlineHearing onlineHearing) {
        return sessionEventRepository.findByOnlineHearing(onlineHearing);
    }

    @Transactional
    public void deleteByOnlineHearing(OnlineHearing onlineHearing) {
        sessionEventRepository.deleteByOnlineHearing(onlineHearing);
    }
}
