package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import javax.persistence.EntityNotFoundException;

@Service
@Component
public class QuestionService {

    private QuestionRepository questionRepository;

    private final QuestionStateService questionStateService;


    @Autowired
    public QuestionService(QuestionRepository questionRepository, QuestionStateService questionStateService) {
        this.questionRepository = questionRepository;
        this.questionStateService = questionStateService;
    }


    public Question retrieveQuestionById(final Long question_id){
        return questionRepository.findById(question_id).orElse(null);
    }

    public Question createQuestion(final int oh_id, final Question question) {
        question.setOnlineHearingId(oh_id);

        question.setQuestionState(questionStateService.retrieveQuestionStateById(QuestionState.DRAFTED));

        return questionRepository.save(question);
    }

    public Question editQuestion(Long questionId, Question body) {
        Question question = retrieveQuestionById(questionId);
        question.addState(questionStateService.retrieveQuestionStateById(QuestionState.ISSUED));
        return questionRepository.save(question);
    }

    public Question updateQuestionById(Question question) throws EntityNotFoundException {

        if (questionRepository.existsById(question.getQuestionId())) {
            return questionRepository.save(question);
        }

        throw new EntityNotFoundException("Could not find the entity with id = " + question.getQuestionId());
    }
}
