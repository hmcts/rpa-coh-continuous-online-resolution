package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.EventForwardingRegister;

import java.util.Optional;

@Repository
public interface EventForwardingRegisterRepository extends CrudRepository<EventForwardingRegister, Integer> {
    Optional<EventForwardingRegister> findByJurisdictionIdAndEventTypeId(Long jurisdictionId, int eventTypeId);
}
