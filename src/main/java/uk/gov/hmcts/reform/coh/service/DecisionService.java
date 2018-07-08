package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.repository.DecisionRepository;

@Service
public class DecisionService {

    private DecisionRepository decisionRepository;

    @Autowired
    public DecisionService(DecisionRepository decisionRepository) {
        this.decisionRepository = decisionRepository;
    }

    public Decision createDecision(Decision decision) {
        return decisionRepository.save(decision);
    }
}
