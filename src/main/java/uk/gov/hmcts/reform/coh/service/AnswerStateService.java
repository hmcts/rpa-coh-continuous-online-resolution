package uk.gov.hmcts.reform.coh.service;

import javassist.NotFoundException;
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

    public boolean validateStateTransition(AnswerState sourceState, AnswerState targetState) throws NotFoundException {

        Optional<AnswerState> submittedAnswerState = retrieveAnswerStateByState("SUBMITTED");
        if(!submittedAnswerState.isPresent()){
            throw new NotFoundException("Submitted state not found");
        }
        Optional<AnswerState> draftedAnswerState = retrieveAnswerStateByState("DRAFTED");
        if(!draftedAnswerState.isPresent()){
            throw new NotFoundException("DRAFTED state not found");
        }
        Optional<AnswerState> editedAnswerState = retrieveAnswerStateByState("answer_edited");
        if(!editedAnswerState.isPresent()){
            throw new NotFoundException("answer_edited state not found");
        }

        if (sourceState.equals(submittedAnswerState.get()) && targetState.equals(draftedAnswerState.get())){
            throw new NotAValidUpdateException();
        }

        if (sourceState.equals(editedAnswerState.get()) && targetState.equals(draftedAnswerState.get())){
            throw new NotAValidUpdateException();
        }

        return true;
    }
}
