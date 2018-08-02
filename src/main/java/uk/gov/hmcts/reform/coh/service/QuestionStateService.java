package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionStateRepository;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import java.util.Optional;

@Service
public class QuestionStateService {

    private QuestionStateRepository questionStateRepository;

    @Autowired
    public QuestionStateService(QuestionStateRepository questionStateRepository) {
        this.questionStateRepository = questionStateRepository;
    }

    public QuestionState retrieveQuestionStateById(final Integer questionStateId){
        return questionStateRepository.findById(questionStateId).orElse(null);
    }

    public Optional<QuestionState> retrieveQuestionStateByStateName(final String stateName){
        return questionStateRepository.findByState(stateName);
    }

    public QuestionState fetchQuestionState(QuestionStates state) {
        return retrieveQuestionStateByStateName(state.getStateName())
            .orElseThrow(() -> new RuntimeException("Unknown question state: " + state.name()));
    }
}