package uk.gov.hmcts.reform.coh.service;

import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.controller.exceptions.NotAValidUpdateException;
import uk.gov.hmcts.reform.coh.domain.AnswerState;
import uk.gov.hmcts.reform.coh.repository.AnswerStateRepository;
import uk.gov.hmcts.reform.coh.states.AnswerStates;

import java.util.Optional;

@Service
public class AnswerStateService {

    private AnswerStateRepository answerStateRepository;

    @Autowired
    public AnswerStateService(AnswerStateRepository answerStateRepository) {
        this.answerStateRepository = answerStateRepository;
    }

    public Optional<AnswerState> retrieveAnswerStateByState(String state) {
        return answerStateRepository.findByState(state);
    }

    public boolean validateStateTransition(AnswerState sourceState, AnswerState targetState) throws NotFoundException {

        Optional<AnswerState> submittedAnswerState = retrieveAnswerStateByState(AnswerStates.SUBMITTED.getStateName());
        if(!submittedAnswerState.isPresent()){
            throw new NotFoundException("Submitted state not found");
        }

        Optional<AnswerState> draftedAnswerState = retrieveAnswerStateByState(AnswerStates.DRAFTED.getStateName());
        if(!draftedAnswerState.isPresent()){
            throw new NotFoundException("Drafted state not found");
        }

        if (sourceState.equals(submittedAnswerState.get())) {
            throw new NotAValidUpdateException("Invalid state transition");
        }

        return true;
    }
}