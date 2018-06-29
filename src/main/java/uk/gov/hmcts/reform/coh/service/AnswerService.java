package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.repository.AnswerRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
@Component
public class AnswerService {

    private AnswerRepository answerRepository;

    private AnswerStateService answerStateService;

    @Autowired
    public AnswerService(AnswerRepository answerRepository, AnswerStateService answerStateService) {
        this.answerRepository = answerRepository;
        this.answerStateService = answerStateService;
    }

    public Answer createAnswer(Answer answer) {
        return answerRepository.save(answer);
    }

    public Optional<Answer> retrieveAnswerById(long answerId) {
        return answerRepository.findById(answerId);
    }

    public List<Answer> retrieveAnswersByQuestion(Question question) {
        return answerRepository.findByQuestion(question);
    }

    public Answer updateAnswerById(Answer answer) throws EntityNotFoundException {

        if (answerRepository.existsById(answer.getAnswerId())) {
            return answerRepository.save(answer);
        }

        throw new EntityNotFoundException("Could not find the entity with id = " + answer.getAnswerId());
    }

    public Answer updateAnswer(Answer source, Answer target) {
        source.setAnswerText(target.getAnswerText());

        if(answerStateService.validateStateTransition(source.getAnswerState(), target.getAnswerState())) {
            source.setAnswerState(target.getAnswerState());
            source.registerStateChange();
            answerRepository.save(source);
        }
        return source;
    }

    @Transactional
    public void deleteAnswer(Answer answer) {
        answerRepository.delete(answer);
    }
}
