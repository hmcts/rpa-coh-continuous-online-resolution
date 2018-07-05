package uk.gov.hmcts.reform.coh.repository;

import org.hibernate.annotations.Entity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OnlineHearingRepository extends CrudRepository<OnlineHearing,UUID> {

    Optional<OnlineHearing> findByCaseId(String externalRef);

    @EntityGraph
    List<OnlineHearing> findByCaseIdAndState(List<String> caseIds, List<String> states);


    @Transactional
    void deleteByCaseId(String externalRef);
}