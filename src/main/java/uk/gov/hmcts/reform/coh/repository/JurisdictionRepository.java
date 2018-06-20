package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.Question;

import java.util.UUID;

@Repository
public interface JurisdictionRepository extends CrudRepository<Question,UUID> {

}