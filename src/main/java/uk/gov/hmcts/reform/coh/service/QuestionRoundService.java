package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.repository.QuestionRoundRepository;

import java.util.Optional;

@Service
@Component
public class QuestionRoundService {

    private QuestionRoundRepository questionRoundRepository;

    @Autowired
    public QuestionRoundService(QuestionRoundRepository questionRoundRepository) {
        this.questionRoundRepository = questionRoundRepository;
    }

    public QuestionRound createQuestionRound(final QuestionRound questionRound) {
        return questionRoundRepository.save(questionRound);
    }

    public Optional<QuestionRound> retrievequestionRoundByExternalRef(final QuestionRound questionRound) {
        return questionRoundRepository.findById(questionRound.getQuestionRoundId());
    }
}
