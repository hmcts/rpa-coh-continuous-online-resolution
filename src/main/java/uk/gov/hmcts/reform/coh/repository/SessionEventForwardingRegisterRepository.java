package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.EventForwardingRegisterId;

@Repository
public interface SessionEventForwardingRegisterRepository extends CrudRepository<SessionEventForwardingRegister, EventForwardingRegisterId> {
}
