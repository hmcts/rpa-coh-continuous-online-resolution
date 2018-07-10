package uk.gov.hmcts.reform.coh.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.Notification.QuestionNotification;
import uk.gov.hmcts.reform.coh.controller.exceptions.NotAValidUpdateException;
import uk.gov.hmcts.reform.coh.domain.Onlinehearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;

@Service
@Component
public class QuestionService {
    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);


    private QuestionRoundService questionRoundService;
    private QuestionRepository questionRepository;
    private final QuestionStateService questionStateService;
    private QuestionNotification questionNotification;
    private OnlinehearingService onlinehearingService;

    @Autowired
    public QuestionService(QuestionRepository questionRepository, QuestionStateService questionStateService,
                           QuestionNotification questionNotification, OnlinehearingService onlinehearingService, QuestionRoundService questionRoundService) {
        this.questionRepository = questionRepository;
        this.questionStateService = questionStateService;
        this.questionNotification = questionNotification;
        this.onlinehearingService = onlinehearingService;
        this.questionRoundService = questionRoundService;
    }

    public Optional<Question> retrieveQuestionById(final UUID question_id){
        return questionRepository.findById(question_id);
    }

    public Question createQuestion(final Question question, UUID onlinehearingId) {
        Onlinehearing onlinehearing = new Onlinehearing();
        onlinehearing.setOnlinehearingId(onlinehearingId);

        Optional<Onlinehearing> optionalOnlinehearing = onlinehearingService.retrieveOnlinehearing(onlinehearing);
        if(!optionalOnlinehearing.isPresent()){
            throw new EntityNotFoundException();
        }

        if(!questionRoundService.validateQuestionRound(question, optionalOnlinehearing.get())) {
            throw new NotAValidUpdateException();
        }

        question.setOnlinehearing(optionalOnlinehearing.get());
        question.setQuestionState(questionStateService.retrieveQuestionStateById(QuestionState.DRAFTED));

        return questionRepository.save(question);
    }

    public Question editQuestion(UUID questionId, Question body) {
        Optional<Question> optionalQuestion = retrieveQuestionById(questionId);
        if (!optionalQuestion.isPresent()) {
            throw new EntityNotFoundException("Question entity not found");
        }
        Question question = optionalQuestion.get();
        question.addState(questionStateService.retrieveQuestionStateById(QuestionState.ISSUED));

        return questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Question question) {
        questionRepository.delete(question);
    }

    public Question updateQuestion(Question currentQuestion, Question updateToQuestion){
        QuestionState questionState = currentQuestion.getQuestionState();

        QuestionState proposedState = updateToQuestion.getQuestionState();

        if(proposedState.getState().equals("ISSUED")) {
            if (questionState.getQuestionStateId() != QuestionState.ISSUED) {
                issueQuestion(currentQuestion);
            }
        }else{
            // Add code to update question text / body ect here (NOT THIS BRANCH)
            questionRepository.save(currentQuestion);
        }
        return currentQuestion;
    }

    protected void issueQuestion(Question question) {
        QuestionState issuedQuestionState = questionStateService.retrieveQuestionStateById(QuestionState.ISSUED);
        
        question.addState(issuedQuestionState);
        boolean result = questionNotification.notifyQuestionState(question);
        if (result){
            log.info("Successfully issued question round and sent notification to jurisdiction");
            questionRepository.save(question);
        }else{
            log.error("Error: Request to jurisdiction was unsuccessful");
        }
    }
}
