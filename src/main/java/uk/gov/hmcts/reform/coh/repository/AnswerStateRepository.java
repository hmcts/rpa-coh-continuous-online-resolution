package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.AnswerState;

import java.util.Optional;

@Repository
public interface AnswerStateRepository extends CrudRepository<AnswerState, Long> {

    Optional<AnswerState> findByState(String state);
}
