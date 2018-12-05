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
import uk.gov.hmcts.reform.coh.util.QuestionEntityUtils;
import uk.gov.hmcts.reform.coh.util.QuestionStateUtils;

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
                .thenReturn(QuestionStateUtils.get(QuestionStates.ISSUED));
        when(questionStateService.fetchQuestionState(QuestionStates.QUESTION_DEADLINE_EXTENSION_GRANTED))
                .thenReturn(QuestionStateUtils.get(QuestionStates.QUESTION_DEADLINE_EXTENSION_GRANTED));
        when(questionStateService.fetchQuestionState(QuestionStates.QUESTION_DEADLINE_EXTENSION_DENIED))
                .thenReturn(QuestionStateUtils.get(QuestionStates.QUESTION_DEADLINE_EXTENSION_DENIED));
    }

    @Test
    public void testIsEligibleForDeadlineExtensionReturnsTrueForIssued() {
        Question mockQuestion = QuestionEntityUtils.createTestQuestion(QuestionStates.ISSUED);
        boolean isEligible = questionDeadlineUtils.isEligibleForDeadlineExtension(mockQuestion);

        assertTrue(isEligible);
    }

    @Test
    public void testIsEligibleForDeadlineExtensionReturnsTrueForQuestionDeadlineExtensionGranted() {
        Question mockQuestion = QuestionEntityUtils.createTestQuestion(QuestionStates.QUESTION_DEADLINE_EXTENSION_GRANTED);
        boolean isEligible = questionDeadlineUtils.isEligibleForDeadlineExtension(mockQuestion);

        assertTrue(isEligible);
    }

    @Test
    public void testIsEligibleForDeadlineExtensionReturnsTrueForQuestionDeadlineExtensionDenied() {
        Question mockQuestion = QuestionEntityUtils.createTestQuestion(QuestionStates.QUESTION_DEADLINE_EXTENSION_DENIED);
        boolean isEligible = questionDeadlineUtils.isEligibleForDeadlineExtension(mockQuestion);

        assertTrue(isEligible);
    }
}