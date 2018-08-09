package uk.gov.hmcts.reform.coh.service.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.service.QuestionStateService;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import java.util.HashSet;
import java.util.Set;

@Component
public class QuestionDeadlineUtils {

    private Set<QuestionState> deadlineEligibleStates;

    @Autowired
    private QuestionStateService questionStateService;

    private void initDeadlineEligibleStates() {
        deadlineEligibleStates = new HashSet<>();
        deadlineEligibleStates.add(questionStateService.fetchQuestionState(QuestionStates.ISSUED));
        deadlineEligibleStates.add(questionStateService.fetchQuestionState(QuestionStates.QUESTION_DEADLINE_EXTENSION_GRANTED));
        deadlineEligibleStates.add(questionStateService.fetchQuestionState(QuestionStates.QUESTION_DEADLINE_EXTENSION_DENIED));
    }

    public boolean isEligibleForDeadlineExtension(Question question) {
        if (deadlineEligibleStates == null) {
            initDeadlineEligibleStates();
        }

        return deadlineEligibleStates.contains(question.getQuestionState());
    }
}
