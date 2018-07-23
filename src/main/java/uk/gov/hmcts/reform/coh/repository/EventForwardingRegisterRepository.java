package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.EventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.EventForwardingRegisterId;
import uk.gov.hmcts.reform.coh.domain.EventType;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;

import java.util.Optional;

@Repository
public interface EventForwardingRegisterRepository extends CrudRepository<EventForwardingRegister, EventForwardingRegisterId> {
    Optional<EventForwardingRegister> findByJurisdictionAndEventType(Jurisdiction jurisdiction, EventType eventType);
}
