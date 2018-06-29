package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.controller.exceptions.NotAValidUpdateException;
import uk.gov.hmcts.reform.coh.domain.AnswerState;
import uk.gov.hmcts.reform.coh.repository.AnswerStateRepository;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class AnswerStateServiceTest {

    @Mock
    private AnswerStateRepository answerStateRepository;

    private AnswerStateService answerStateService;

    private static final Long ONE = 1L;

    private AnswerState draftedState;
    private AnswerState editedState;
    private AnswerState submittedState;

    @Before
    public void setup() {
        draftedState = new AnswerState();
        draftedState.setState("DRAFTED");
        draftedState.setAnswerStateId(1);

        editedState = new AnswerState();
        editedState.setState("answer_edited");
        editedState.setAnswerStateId(2);

        submittedState = new AnswerState();
        submittedState.setState("SUBMITTED");
        submittedState.setAnswerStateId(3);

        answerStateService = new AnswerStateService(answerStateRepository);
        when(answerStateRepository.findByState("DRAFTED")).thenReturn(Optional.of(draftedState));
        when(answerStateRepository.findByState("SUBMITTED")).thenReturn(Optional.of(submittedState));
        when(answerStateRepository.findByState("answer_edited")).thenReturn(Optional.of(editedState));
    }

    @Test
    public void testRetrieveAnswerState() {
        when(answerStateRepository.findByState("DRAFTED")).thenReturn(Optional.of(draftedState));

        AnswerState newAnswerState = answerStateService.retrieveAnswerStateByState("DRAFTED").get();
        assertEquals(draftedState, newAnswerState);
    }

    @Test
    public void testValidateStateUpdateServiceDraftedToEdited(){
        boolean valid = answerStateService.validateStateTransition(draftedState, editedState);
        assertTrue(valid);
    }

    @Test
    public void testValidateStateUpdateServiceEditedToSubmitted(){
        boolean valid = answerStateService.validateStateTransition(editedState, submittedState);
        assertTrue(valid);
    }

    @Test(expected = NotAValidUpdateException.class)
    public void testValidateStateUpdateSubmittedToDrafted(){
        answerStateService.validateStateTransition(submittedState, draftedState);
    }

    @Test(expected = NotAValidUpdateException.class)
    public void testValidateStateUpdateEditedToDrafted(){
        boolean valid = answerStateService.validateStateTransition(editedState, draftedState);
    }
}
