package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.domain.Onlinehearing;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OnlinehearingRepository extends CrudRepository<Onlinehearing,UUID> {

    Optional<Onlinehearing> findByCaseId(String caseId);

    @Transactional
    void deleteByCaseId(String caseId);
}