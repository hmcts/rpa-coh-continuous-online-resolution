package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.domain.DecisionReply;
import uk.gov.hmcts.reform.coh.repository.DecisionReplyRepository;

@Service
public class DecisionReplyService {

    private DecisionReplyRepository decisionReplyRepository;

    @Autowired
    public DecisionReplyService(DecisionReplyRepository decisionReplyRepository) {
        this.decisionReplyRepository = decisionReplyRepository;
    }

    @Transactional
    public DecisionReply createDecision(DecisionReply decisionReply) {
        return decisionReplyRepository.save(decisionReply);
    }
}
