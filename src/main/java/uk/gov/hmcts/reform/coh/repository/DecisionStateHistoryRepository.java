package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionStateHistory;

import java.util.List;

@Repository
public interface DecisionStateHistoryRepository extends CrudRepository<DecisionStateHistory, Long> {

    List<DecisionStateHistory> findAllByDecision(Decision decision);
}
