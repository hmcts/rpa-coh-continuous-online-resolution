package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;
import uk.gov.hmcts.reform.coh.repository.QuestionStateRepository;

import java.util.UUID;

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


    public Question retrieveQuestionById(final int question_id){
        return questionRepository.findById(question_id).orElse(null);
    }

    public Question createQuestion(final int oh_id, final int qr_id, final Question question) {
        question.setOnlineHearingId(oh_id);
        question.setQuestionRoundId(qr_id);

        question.addState(questionStateService.retrieveQuestionStateById(1));
        return questionRepository.save(question);
    }
}