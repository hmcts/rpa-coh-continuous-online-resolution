package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import uk.gov.hmcts.reform.coh.Notification.QuestionNotification;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;

@Service
@Component
public class QuestionService {

    private QuestionRepository questionRepository;
    private final QuestionStateService questionStateService;
    private QuestionNotification questionNotification;
    private OnlineHearingService onlineHearingService;

    @Autowired
    public QuestionService(QuestionRepository questionRepository, QuestionStateService questionStateService, QuestionNotification questionNotification, OnlineHearingService onlineHearingService) {
        this.questionRepository = questionRepository;
        this.questionStateService = questionStateService;
        this.questionNotification = questionNotification;
        this.onlineHearingService = onlineHearingService;
    }

    public Question retrieveQuestionById(final Long question_id){
        return questionRepository.findById(question_id).orElse(null);
    }

    public Question createQuestion(final Question question, UUID onlineHearingId) {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingId);

        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearing);
        if(!optionalOnlineHearing.isPresent()){
            throw new EntityNotFoundException();
        }

        question.setOnlineHearing(optionalOnlineHearing.get());
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
        QuestionState issuedQuestionState = questionStateService.retrieveQuestionStateById(QuestionState.ISSUED);

        boolean success = updateQuestionState(question, issuedQuestionState);
        if(success){
            System.out.println("Successfully issued question round and sent notification to jurisdiction");
            return true;
        }else{
            System.out.println("Request to jurisdiction was unsuccessful");
            return false;
        }
    }

    protected boolean updateQuestionState(Question question, QuestionState questionState) throws ResourceAccessException{
        question.addState(questionState);
        boolean result = questionNotification.notifyQuestionState(question);

        if (result){
            questionRepository.save(question);
            return true;
        }else {
            return false;
        }
    }
}
