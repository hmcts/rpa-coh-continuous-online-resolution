package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionReply;

import java.util.List;
import java.util.UUID;

@Repository
public interface DecisionReplyRepository extends CrudRepository<DecisionReply, UUID> {
    List<DecisionReply> findAllByDecision(Decision decision);
}
