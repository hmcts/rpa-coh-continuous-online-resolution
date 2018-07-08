package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.DecisionState;

import java.util.Optional;

@Repository
public interface DecisionStateRepository extends CrudRepository<DecisionState, Long> {

    Optional<DecisionState> findByState(String state);
}
