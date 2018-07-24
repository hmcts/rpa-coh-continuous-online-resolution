package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
            throw new EntityNotFoundException("Session Event Type '" + sessionEventType + "' not found");
        }

        return createSessionEvent(onlineHearing, optSessionEventType.get());
    }

    public SessionEvent createSessionEvent(OnlineHearing onlineHearing, SessionEventType sessionEventType) {

        Optional<SessionEventForwardingState> optionalForwardingState = sessionEventForwardingStateRepository.findByForwardingStateName(STARTING_STATE);
        if (!optionalForwardingState.isPresent()) {
            throw new EntityNotFoundException("Session Event Forwarding State '" + STARTING_STATE + "' not found");
        }


        Optional<SessionEventForwardingRegister> optRegister =  sessionEventForwardingRegisterRepository.findByJurisdictionAndSessionEventType(onlineHearing.getJurisdiction(), sessionEventType);
        if (!optRegister.isPresent()) {
            throw new EntityNotFoundException("Session event regist entry not found");
        }

        SessionEvent sessionEvent = new SessionEvent();
        sessionEvent.setOnlineHearing(onlineHearing);
        sessionEvent.setSessionEventForwardingState(optionalForwardingState.get());

        return sessionEventRepository.save(null);
    }
}
