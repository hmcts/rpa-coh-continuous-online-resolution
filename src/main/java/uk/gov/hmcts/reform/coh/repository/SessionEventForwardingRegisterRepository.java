package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegisterId;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;

import java.util.Optional;

@Repository
public interface SessionEventForwardingRegisterRepository
        extends CrudRepository<SessionEventForwardingRegister, SessionEventForwardingRegisterId> {

    Optional<SessionEventForwardingRegister> findByJurisdictionAndSessionEventType(Jurisdiction jurisdiction, SessionEventType sessionEventType);
}
