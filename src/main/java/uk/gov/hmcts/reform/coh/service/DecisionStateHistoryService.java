package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionStateHistory;
import uk.gov.hmcts.reform.coh.repository.DecisionStateHistoryRepository;

import java.util.List;

@Service
public class DecisionStateHistoryService {

    private DecisionStateHistoryRepository decisionStateHistoryRepository;

    @Autowired
    public DecisionStateHistoryService(DecisionStateHistoryRepository decisionStateHistoryRepository) {
        this.decisionStateHistoryRepository = decisionStateHistoryRepository;
    }

    public List<DecisionStateHistory> findAllByDecision(Decision decision) {
        return decisionStateHistoryRepository.findAllByDecision(decision);
    }
}
