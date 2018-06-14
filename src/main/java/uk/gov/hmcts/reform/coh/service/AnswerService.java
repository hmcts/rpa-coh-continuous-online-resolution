package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.repository.AnswerRepository;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
@Component
public class AnswerService {

    private AnswerRepository answerRepository;

    @Autowired
    public AnswerService(AnswerRepository answerRepository) {
        this.answerRepository = answerRepository;
    }

    public Answer createAnswer(Answer answer) {
        return answerRepository.save(answer);
    }

    public Optional<Answer> retrieveAnswerById(long answerId) {
        return answerRepository.findById(answerId);
    }

    public Answer updateAnswerById(Answer answer) throws EntityNotFoundException {

        if (answerRepository.existsById(answer.getAnswerId())) {
            return answerRepository.save(answer);
        }

        throw new EntityNotFoundException("Could not find the entity with id = " + answer.getAnswerId());
    }
}
