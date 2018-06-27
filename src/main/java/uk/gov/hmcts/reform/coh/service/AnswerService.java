package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.repository.AnswerRepository;

import javax.persistence.EntityNotFoundException;
import java.util.InputMismatchException;
import java.util.List;
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

    public List<Answer> retrieveAnswersByQuestion(Question question) {
        return answerRepository.findByQuestion(question);
    }

    public Answer updateAnswerById(Answer answer) throws EntityNotFoundException {

        if (answerRepository.existsById(answer.getAnswerId())) {
            return answerRepository.save(answer);
        }

        throw new EntityNotFoundException("Could not find the entity with id = " + answer.getAnswerId());
    }

    public Answer editAnswer(Answer body){
        Optional<Answer> optAnswer = answerRepository.findById(body.getAnswerId());
        if(optAnswer.isPresent()){
            return updateAnswer(optAnswer.get(), body);
        }
        throw new EntityNotFoundException("Could not find the entity with id = " + body.getAnswerId());
    }

    protected Answer updateAnswer(Answer source, Answer target){
        if(source.getAnswerId().equals(target.getAnswerId())) {
            source.setAnswerText(target.getAnswerText());
            source.addState(target.getAnswerState());
            answerRepository.save(source);
            return source;
        }
        throw new InputMismatchException("Could not match answer with request");
    }

    @Transactional
    public void deleteAnswer(Answer answer) {
        answerRepository.delete(answer);
    }
}
