package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegisterId;

@Repository
public interface SessionEventForwardingRegisterRepository
        extends CrudRepository<SessionEventForwardingRegister, SessionEventForwardingRegisterId> {
}
