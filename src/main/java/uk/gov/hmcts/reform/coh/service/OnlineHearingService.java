package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@Component
public class OnlineHearingService {

    private OnlineHearingRepository onlineHearingRepository;
    private OnlineHearingStateService onlineHearingStateService;

    @Autowired
    public OnlineHearingService(OnlineHearingRepository onlineHearingRepository,
                                OnlineHearingStateService onlineHearingStateService) {
        this.onlineHearingRepository = onlineHearingRepository;
        this.onlineHearingStateService = onlineHearingStateService;
    }

    public OnlineHearingService(OnlineHearingRepository onlineHearingRepository) {
    }

    public OnlineHearing createOnlineHearing(final OnlineHearing onlineHearing) {
        return onlineHearingRepository.save(onlineHearing);
    }

    public Optional<OnlineHearing> retrieveOnlineHearing(final OnlineHearing onlineHearing) {
        return onlineHearingRepository.findById(onlineHearing.getOnlineHearingId());
    }

    public OnlineHearing retrieveOnlineHearingById(final UUID onlineHearingId) {
        return onlineHearingRepository.findById(onlineHearingId).orElse(null);
    }

    public void deleteOnlineHearing(final OnlineHearing onlineHearing) {
        onlineHearingRepository.delete(onlineHearing);
    }

    public void deleteById(UUID id) {
        onlineHearingRepository.deleteById(id);
    }

    public OnlineHearing updateOnlineHearingState(UUID id, OnlineHearing body) {
        OnlineHearing onlineHearing = retrieveOnlineHearingById(id);
        onlineHearing.addState(onlineHearingStateService.retrieveOnlineHearingStateById(OnlineHearingState.CLOSED));
        return onlineHearingRepository.save(onlineHearing);
    }

}