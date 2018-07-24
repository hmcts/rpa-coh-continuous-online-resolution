package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingState;

import java.util.Optional;

@Repository
public interface SessionEventForwardingStateRepository extends CrudRepository<SessionEventForwardingState, Integer> {
    Optional<SessionEventForwardingState> findByForwardingStateName(String stateName);
}
