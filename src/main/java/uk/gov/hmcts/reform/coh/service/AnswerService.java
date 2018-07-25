package uk.gov.hmcts.reform.coh.service;

import javassist.NotFoundException;
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
import java.util.UUID;

@Service
public class AnswerService {

    private AnswerRepository answerRepository;

    private AnswerStateService answerStateService;

    @Autowired
    public AnswerService(AnswerRepository answerRepository, AnswerStateService answerStateService) {
        this.answerRepository = answerRepository;
        this.answerStateService = answerStateService;
    }

    @Transactional
    public Answer createAnswer(Answer answer) {
        return answerRepository.save(answer);
    }

    @Transactional
    public Optional<Answer> retrieveAnswerById(UUID answerId) {
        return answerRepository.findById(answerId);
    }

    @Transactional
    public List<Answer> retrieveAnswersByQuestion(Question question) {
        return answerRepository.findByQuestion(question);
    }

    @Transactional
    public Answer updateAnswerById(Answer answer) throws EntityNotFoundException {

        if (answerRepository.existsById(answer.getAnswerId())) {
            return answerRepository.save(answer);
        }

        throw new EntityNotFoundException("Could not find the entity with id = " + answer.getAnswerId());
    }

    @Transactional
    public Answer updateAnswer(Answer source, Answer target) throws NotFoundException {
        source.setAnswerText(target.getAnswerText());

        if (answerStateService.validateStateTransition(source.getAnswerState(), target.getAnswerState())) {
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
