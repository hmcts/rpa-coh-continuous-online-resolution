package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;
import uk.gov.hmcts.reform.coh.repository.QuestionStateRepository;

@Service
@Component
public class QuestionStateService {

    private QuestionStateRepository questionStateRepository;

    @Autowired
    public QuestionStateService(QuestionStateRepository questionStateRepository) {
        this.questionStateRepository = questionStateRepository;
    }


    public QuestionState retrieveQuestionStateById(final int questionStateId){
        System.out.println("questionStateId " + questionStateId);
        QuestionState questionState1 = (QuestionState) questionStateRepository.findAll();
        System.out.println("questionState1 " + questionState1);
        QuestionState questionState = questionStateRepository.findById(questionStateId).orElse(null);
        System.out.println("questionState " + questionState);
        return questionState;
    }

}