package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.controller.exceptions.NotAValidUpdateException;
import uk.gov.hmcts.reform.coh.domain.AnswerState;
import uk.gov.hmcts.reform.coh.repository.AnswerStateRepository;

import java.util.Optional;

@Service
@Component
public class AnswerStateService {

    private AnswerStateRepository answerStateRepository;

    @Autowired
    public AnswerStateService(AnswerStateRepository answerStateRepository) {
        this.answerStateRepository = answerStateRepository;
    }

    public Optional<AnswerState> retrieveAnswerStateByState(String state) {
        return answerStateRepository.findByState(state);
    }

    public boolean validateStateTransition(AnswerState sourceState, AnswerState targetState){
        AnswerState submittedAnswerState = retrieveAnswerStateByState("SUBMITTED").get();
        AnswerState draftedAnswerState = retrieveAnswerStateByState("DRAFTED").get();
        AnswerState editedAnswerState = retrieveAnswerStateByState("answer_edited").get();

        if (sourceState.equals(submittedAnswerState) && targetState.equals(draftedAnswerState)){
            throw new NotAValidUpdateException();
        }

        if (sourceState.equals(editedAnswerState) && targetState.equals(draftedAnswerState)){
            throw new NotAValidUpdateException();
        }

        return true;
    }
}
