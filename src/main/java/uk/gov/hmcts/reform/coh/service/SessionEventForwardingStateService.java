package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingState;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingStateRepository;

import java.util.Optional;

@Service
public class SessionEventForwardingStateService {

    private SessionEventForwardingStateRepository sessionEventForwardingStateRepository;

    @Autowired
    public SessionEventForwardingStateService(SessionEventForwardingStateRepository sessionEventForwardingStateRepository) {
        this.sessionEventForwardingStateRepository = sessionEventForwardingStateRepository;
    }

    public Optional<SessionEventForwardingState> retrieveEventForwardingStateByName(String stateName) {
        return sessionEventForwardingStateRepository.findByForwardingStateName(stateName);
    }
}
