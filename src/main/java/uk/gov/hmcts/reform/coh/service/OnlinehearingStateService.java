package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.Onlinehearingstate;
import uk.gov.hmcts.reform.coh.repository.OnlinehearingStateRepository;

import java.util.Optional;

@Service
@Component
public class OnlinehearingStateService {

    private OnlinehearingStateRepository onlinehearingStateRepository;

    @Autowired
    public OnlinehearingStateService(OnlinehearingStateRepository onlinehearingStateRepository) {
        this.onlinehearingStateRepository = onlinehearingStateRepository;
    }

    public Optional<Onlinehearingstate> retrieveOnlinehearingStateByState(String state){
        return onlinehearingStateRepository.findByState(state);
    }

    public Onlinehearingstate retrieveOnlinehearingStateById(final int onlinehearingStateId){
        return onlinehearingStateRepository.findById(onlinehearingStateId).orElse(null);
    }

}
