package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;

@Repository
public interface SessionEventTypeRespository extends CrudRepository<SessionEventType, Integer> {
}
