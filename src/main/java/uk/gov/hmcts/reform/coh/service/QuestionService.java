package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import uk.gov.hmcts.reform.coh.Notification.QuestionNotification;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import javax.persistence.EntityNotFoundException;

@Service
@Component
public class QuestionService {

    private QuestionRepository questionRepository;
    private final QuestionStateService questionStateService;
    private QuestionNotification questionNotification;

    @Autowired
    public QuestionService(QuestionRepository questionRepository, QuestionStateService questionStateService, QuestionNotification questionNotification) {
        this.questionRepository = questionRepository;
        this.questionStateService = questionStateService;
        this.questionNotification = questionNotification;
    }

    public Question retrieveQuestionById(final Long question_id){
        return questionRepository.findById(question_id).orElse(null);
    }

    public Question createQuestion(final Question question) {
        //question.setOnlineHearingId(oh_id);

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

    public boolean issueQuestion(Question question) {
        boolean success = false;

        QuestionState issuedQuestionState = questionStateService.retrieveQuestionStateById(QuestionState.ISSUED);
        try {
            success = updateQuestionState(question, issuedQuestionState);
        }catch(ResourceAccessException e){
            System.out.println(e);
        }finally {
            if(success){
                System.out.println("Successfully issued question round and sent notification to jurisdiction");
                return true;
            }else{
                System.out.println("Request to jurisdiction was unsuccessful");
                return false;
            }
        }
    }

    protected boolean updateQuestionState(Question question, QuestionState questionState) throws ResourceAccessException{
        try {
            question.addState(questionState);
            if (questionNotification.notifyQuestionState(question)){
                questionRepository.save(question);
                return true;
            }else {
                return false;
            }
        }catch(ResourceAccessException e){
            throw e;
        }
    }
}
