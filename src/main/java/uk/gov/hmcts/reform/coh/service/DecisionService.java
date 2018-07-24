package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.repository.DecisionRepository;

import java.util.*;

@Service
public class DecisionService {

    private DecisionRepository decisionRepository;

    @Autowired
    public DecisionService(DecisionRepository decisionRepository) {
        this.decisionRepository = decisionRepository;
    }

    @Transactional
    public Decision createDecision(Decision decision) {
        return decisionRepository.save(decision);
    }

    @Transactional
    public Optional<Decision> findByOnlineHearingId(UUID onlineHearingId) {
        return decisionRepository.findByOnlineHearingOnlineHearingId(onlineHearingId);
    }

    public Optional<Decision> retrieveByOnlineHearingIdAndDecisionId(UUID onlineHearingId, UUID decisionId) {
        return decisionRepository.findByOnlineHearingOnlineHearingIdAndDecisionId(onlineHearingId, decisionId);
    }

    @Transactional
    public Decision updateDecision(Decision decision) {
        return decisionRepository.save(decision);
    }

    @Transactional
    public void deleteDecisionById(UUID decisionId) {
        decisionRepository.deleteById(decisionId);
    }
}
