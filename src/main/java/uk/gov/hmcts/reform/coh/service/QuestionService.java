package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
@Component
public class QuestionService {

    private QuestionRepository questionRepository;

    @Autowired
    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public Question createQuestion(Question question) {
        return questionRepository.save(question);
    }

    public Optional<Question> retrieveQuestionById(long questionId) {
        return questionRepository.findById(questionId);
    }

    public Question updateQuestionById(Question question) throws EntityNotFoundException {

        if (questionRepository.existsById(question.getQuestionId())) {
            return questionRepository.save(question);
        }

        throw new EntityNotFoundException("Could not find the entity with id = " + question.getQuestionId());
    }
}
