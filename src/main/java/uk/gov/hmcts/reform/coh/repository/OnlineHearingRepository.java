package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OnlineHearingRepository extends CrudRepository<OnlineHearing,UUID> {

    Optional<OnlineHearing> findByCaseId(String caseId);

    List<OnlineHearing> findAllByCaseIdIn(List<String> caseIds);

    @Transactional
    void deleteByCaseId(String externalRef);
}