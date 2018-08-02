package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionStateRepository;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class QuestionStateServiceTest {

    @Mock
    private QuestionStateRepository questionStateRepository;

    private QuestionStateService questionStateService;

    private QuestionState questionState = new QuestionState("DRAFTED");

    @Before
    public void setup() {
        questionStateService = new QuestionStateService(questionStateRepository);
    }

    @Test
    public void testRetrieveQuestionStateById() {
        when(questionStateRepository.findById(1)).thenReturn(Optional.of(questionState));

        /**
         * TODO - Service should be return Optional
         */
        QuestionState newQuestionState = questionStateService.retrieveQuestionStateById(1);
        assertEquals(questionState, newQuestionState);
    }

    @Test
    public void testRetrieveStateByStateName() {
        when(questionStateRepository.findByState(anyString())).thenReturn(Optional.ofNullable(questionState));
        Optional<QuestionState> questionState = questionStateService.retrieveQuestionStateByStateName("ISSUED");
        assertTrue(questionState.isPresent());
    }

    @Test
    public void testFetchExistingState() {
        when(questionStateRepository.findByState(QuestionStates.DRAFTED.getStateName()))
            .thenReturn(Optional.of(questionState));

        QuestionState draftedState = questionStateService.fetchQuestionState(QuestionStates.DRAFTED);
        assertEquals(questionState, draftedState);
    }

    @Test(expected = RuntimeException.class)
    public void testThrowExceptionWhenFetchingNonExistingState() {
        when(questionStateRepository.findByState(anyString())).thenReturn(Optional.empty());
        questionStateService.fetchQuestionState(QuestionStates.ISSUED);
    }

    @Test(expected = NullPointerException.class)
    public void testFetchNullState() {
        questionStateService.fetchQuestionState(null);
    }
}
