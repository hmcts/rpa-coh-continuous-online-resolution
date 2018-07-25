package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingStateRepository;

import java.util.Optional;

@Service
public class OnlineHearingStateService {

    private OnlineHearingStateRepository onlineHearingStateRepository;

    @Autowired
    public OnlineHearingStateService(OnlineHearingStateRepository onlineHearingStateRepository) {
        this.onlineHearingStateRepository = onlineHearingStateRepository;
    }

    public Optional<OnlineHearingState> retrieveOnlineHearingStateByState(String state){
        return onlineHearingStateRepository.findByState(state);
    }

    public OnlineHearingState retrieveOnlineHearingStateById(final int onlineHearingStateId){
        return onlineHearingStateRepository.findById(onlineHearingStateId).orElse(null);
    }

}
