package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.SessionEvent;

import java.util.UUID;

@Repository
public interface SessionEventRepository extends CrudRepository<SessionEvent, UUID> {
}
