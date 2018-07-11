package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.Decision;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DecisionRepository extends CrudRepository<Decision, UUID> {

    Optional<Decision> findByOnlineHearingOnlineHearingId(UUID onlineHearingId);

    Optional<Decision> findByOnlineHearingOnlineHearingIdAndDecisionId(UUID onlineHearingId, UUID decisionId);
}
