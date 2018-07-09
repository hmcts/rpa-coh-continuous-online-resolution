package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionStateRepository;

@Service
@Component
public class QuestionStateService {

    private QuestionStateRepository questionStateRepository;

    @Autowired
    public QuestionStateService(QuestionStateRepository questionStateRepository) {
        this.questionStateRepository = questionStateRepository;
    }

    public QuestionState retrieveQuestionStateById(final Integer questionStateId){
        return questionStateRepository.findById(questionStateId).orElse(null);
    }


}