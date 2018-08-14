package uk.gov.hmcts.reform.coh.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.QuestionRoundService;
import uk.gov.hmcts.reform.coh.service.QuestionStateService;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class QuestionRoundSentTask implements ContinuousOnlineResolutionTask<OnlineHearing> {

    private static final Logger log = LoggerFactory.getLogger(QuestionRoundSentTask.class);

    private QuestionRoundService questionRoundService;

    private QuestionStateService questionStateService;

    @Autowired
    public QuestionRoundSentTask(
        QuestionRoundService questionRoundService,
        QuestionStateService questionStateService
    ) {
        this.questionRoundService = questionRoundService;
        this.questionStateService = questionStateService;
    }

    @Override
    @Transactional
    public void execute(OnlineHearing onlineHearing) {
        Optional<QuestionState> optionalQuestionState = questionStateService.retrieveQuestionStateByStateName(QuestionStates.ISSUED.getStateName());
        if (!optionalQuestionState.isPresent()) {
            log.debug("Unable to find question state: " + QuestionStates.ISSUED.getStateName());
            return;
        }

        QuestionState questionSentState = optionalQuestionState.get();

        List<Question> qrQuestions = questionRoundService.getQuestionsByQuestionRound(onlineHearing, questionRoundService.getCurrentQuestionRoundNumber(onlineHearing));
        questionRoundService.issueQuestionRound(optionalQuestionState.get(), qrQuestions);
        log.debug("QuestionSentTask.execute(). Online Hearing state update state updated to " + questionSentState.getState());
    }

    @Override
    public List<String> supports() {
        return Arrays.asList(EventTypes.QUESTION_ROUND_ISSUED.getEventType());
    }
}