package uk.gov.hmcts.reform.coh.service.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.service.QuestionStateService;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class QuestionDeadlineUtilsTest {

    @Mock
    private QuestionStateService questionStateService;

    @InjectMocks
    private QuestionDeadlineUtils questionDeadlineUtils;

    @Before
    public void setUp() {
        when(questionStateService.fetchQuestionState(QuestionStates.ISSUED))
                .thenReturn(new QuestionState(QuestionStates.ISSUED.getStateName()));
        when(questionStateService.fetchQuestionState(QuestionStates.QUESTION_DEADLINE_EXTENSION_GRANTED))
                .thenReturn(new QuestionState(QuestionStates.QUESTION_DEADLINE_EXTENSION_GRANTED.getStateName()));
        when(questionStateService.fetchQuestionState(QuestionStates.QUESTION_DEADLINE_EXTENSION_DENIED))
                .thenReturn(new QuestionState(QuestionStates.QUESTION_DEADLINE_EXTENSION_DENIED.getStateName()));
    }

    @Test
    public void testIsEligibleForDeadlineExtensionReturnsTrueForIssued() {
        Question mockQuestion = new Question();
        mockQuestion.setQuestionState(new QuestionState(QuestionStates.ISSUED.getStateName()));
        boolean isEligible = questionDeadlineUtils.isEligibleForDeadlineExtension(mockQuestion);

        assertTrue(isEligible);
    }

    @Test
    public void testIsEligibleForDeadlineExtensionReturnsTrueForQuestionDeadlineExtensionGranted() {
        Question mockQuestion = new Question();
        mockQuestion.setQuestionState(new QuestionState(QuestionStates.QUESTION_DEADLINE_EXTENSION_GRANTED.getStateName()));
        boolean isEligible = questionDeadlineUtils.isEligibleForDeadlineExtension(mockQuestion);

        assertTrue(isEligible);
    }

    @Test
    public void testIsEligibleForDeadlineExtensionReturnsTrueForQuestionDeadlineExtensionDenied() {
        Question mockQuestion = new Question();
        mockQuestion.setQuestionState(new QuestionState(QuestionStates.QUESTION_DEADLINE_EXTENSION_DENIED.getStateName()));
        boolean isEligible = questionDeadlineUtils.isEligibleForDeadlineExtension(mockQuestion);

        assertTrue(isEligible);
    }
}