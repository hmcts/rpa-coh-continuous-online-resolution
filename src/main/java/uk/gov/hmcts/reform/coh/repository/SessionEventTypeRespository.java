package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.SessionEvent;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;

import java.util.Optional;

@Repository
public interface SessionEventTypeRespository extends CrudRepository<SessionEventType, Integer> {

    Optional<SessionEventType> findByEventTypeName(String eventTypeName);
}
