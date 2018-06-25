package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import javax.persistence.EntityNotFoundException;
import java.util.UUID;

@Service
@Component
public class QuestionService {

    private QuestionRepository questionRepository;

    private final QuestionStateService questionStateService;

    private final OnlineHearingService onlineHearingService;


    @Autowired
    public QuestionService(QuestionRepository questionRepository, QuestionStateService questionStateService, OnlineHearingService onlineHearingService) {
        this.questionRepository = questionRepository;
        this.questionStateService = questionStateService;
        this.onlineHearingService = onlineHearingService;
    }


    public Question retrieveQuestionById(final Long question_id){
        return questionRepository.findById(question_id).orElse(null);
    }

    public Question createQuestion(final UUID onlineHearingId, final Question question) {
        OnlineHearing onlineHearing = onlineHearingService.retrieveOnlineHearingById(onlineHearingId);
        question.setOnlineHearing(onlineHearing);

        question.addState(questionStateService.retrieveQuestionStateById(QuestionState.DRAFTED));

        return questionRepository.save(question);
    }

    public Question editQuestion(Long questionId, Question body) {
        Question question = retrieveQuestionById(questionId);
        question.addState(questionStateService.retrieveQuestionStateById(QuestionState.ISSUED));
        return questionRepository.save(question);
    }
}