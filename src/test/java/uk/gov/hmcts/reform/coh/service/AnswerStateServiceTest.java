package uk.gov.hmcts.reform.coh.service;

import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.controller.exceptions.NotAValidUpdateException;
import uk.gov.hmcts.reform.coh.domain.AnswerState;
import uk.gov.hmcts.reform.coh.repository.AnswerStateRepository;
import uk.gov.hmcts.reform.coh.states.AnswerStates;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class AnswerStateServiceTest {

    @Mock
    private AnswerStateRepository answerStateRepository;

    private AnswerStateService answerStateService;

    private AnswerState draftedState;
    private AnswerState submittedState;

    @Before
    public void setup() {
        draftedState = new AnswerState();
        draftedState.setState(AnswerStates.DRAFTED.getStateName());
        draftedState.setAnswerStateId(1);

        submittedState = new AnswerState();
        submittedState.setState((AnswerStates.SUBMITTED.getStateName()));
        submittedState.setAnswerStateId(3);

        answerStateService = new AnswerStateService(answerStateRepository);
        when(answerStateRepository.findByState(AnswerStates.DRAFTED.getStateName())).thenReturn(Optional.of(draftedState));
        when(answerStateRepository.findByState(AnswerStates.SUBMITTED.getStateName())).thenReturn(Optional.of(submittedState));
    }

    @Test(expected = NotFoundException.class)
    public void testRetrievingSubmittedStateFailure() throws NotFoundException{
        when(answerStateRepository.findByState(AnswerStates.SUBMITTED.getStateName())).thenReturn(Optional.empty());
        answerStateService.validateStateTransition(draftedState, submittedState);
    }
    @Test
    public void testRetrieveAnswerState() {
        when(answerStateRepository.findByState(AnswerStates.DRAFTED.getStateName())).thenReturn(Optional.of(draftedState));

        AnswerState newAnswerState = answerStateService.retrieveAnswerStateByState(AnswerStates.DRAFTED.getStateName()).get();
        assertEquals(draftedState, newAnswerState);
    }

    @Test(expected = NotAValidUpdateException.class)
    public void testValidateStateUpdateSubmittedToDrafted() throws NotFoundException {
        answerStateService.validateStateTransition(submittedState, draftedState);
    }
}
