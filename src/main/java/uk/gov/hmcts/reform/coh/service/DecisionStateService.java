package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.DecisionState;
import uk.gov.hmcts.reform.coh.repository.DecisionStateRepository;

import java.util.Optional;

@Service
public class DecisionStateService {

    private DecisionStateRepository decisionStateRepository;

    @Autowired
    public DecisionStateService(DecisionStateRepository decisionStateRepository) {
        this.decisionStateRepository = decisionStateRepository;
    }

    public Optional<DecisionState> retrieveDecisionStateByState(String state) {
        return decisionStateRepository.findByState(state);
    }
}
