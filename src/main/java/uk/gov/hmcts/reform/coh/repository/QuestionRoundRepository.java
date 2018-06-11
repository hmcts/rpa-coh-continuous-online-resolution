package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;

@Repository
public interface QuestionRoundRepository extends CrudRepository<QuestionRound, Long> {
}
