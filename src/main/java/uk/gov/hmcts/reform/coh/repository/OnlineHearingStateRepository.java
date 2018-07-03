package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;

import java.util.Optional;

@Repository
public interface OnlineHearingStateRepository extends CrudRepository<OnlineHearingState, Integer> {
    Optional<OnlineHearingState> findByState(String state);
}