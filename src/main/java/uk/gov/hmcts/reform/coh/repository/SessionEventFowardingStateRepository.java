package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingState;

@Repository
public interface SessionEventFowardingStateRepository extends CrudRepository<SessionEventForwardingState, Integer> {
}
