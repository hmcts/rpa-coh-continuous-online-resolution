package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends CrudRepository<Question, UUID> {
    List<Question> findAllByOnlineHearingOrderByQuestionRoundDesc(OnlineHearing onlineHearing);
    List<Question> findByOnlineHearingAndQuestionRound(OnlineHearing onlineHearing, Integer questionRound);
}