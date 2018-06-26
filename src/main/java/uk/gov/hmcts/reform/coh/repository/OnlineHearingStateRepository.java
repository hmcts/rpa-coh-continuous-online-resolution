package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingStateType;

@Repository
public interface OnlineHearingStateRepository extends CrudRepository<OnlineHearingStateType, Integer> {
}
