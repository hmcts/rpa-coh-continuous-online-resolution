package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.QuestionState;

import java.util.Optional;

@Repository
public interface QuestionStateRepository extends CrudRepository<QuestionState,Integer> {

    Optional<QuestionState> findByState(String stateName);
}