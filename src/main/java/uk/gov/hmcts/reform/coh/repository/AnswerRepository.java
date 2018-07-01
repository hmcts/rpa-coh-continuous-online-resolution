package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.Question;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnswerRepository extends CrudRepository<Answer, UUID> {

    List<Answer> findByQuestion(Question question);
}
