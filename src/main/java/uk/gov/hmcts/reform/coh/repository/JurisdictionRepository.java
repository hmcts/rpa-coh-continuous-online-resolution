package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;

import java.util.Optional;

@Repository
public interface JurisdictionRepository extends CrudRepository<Jurisdiction, Integer> {
    Optional<Jurisdiction> findByJurisdictionName(String jurisdictionName);
}
