package uk.gov.hmcts.reform.coh.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;
import uk.gov.hmcts.reform.coh.service.QuestionRoundService;
import uk.gov.hmcts.reform.coh.service.QuestionStateService;
import uk.gov.hmcts.reform.coh.states.OnlineHearingStates;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class QuestionRoundSentTask implements ContinuousOnlineResolutionTask<OnlineHearing> {

    private static final Logger log = LoggerFactory.getLogger(QuestionRoundSentTask.class);

    private OnlineHearingService onlineHearingService;

    private OnlineHearingStateService onlineHearingStateService;

    private QuestionRoundService questionRoundService;

    private QuestionStateService questionStateService;

    @Autowired
    public QuestionRoundSentTask(OnlineHearingService onlineHearingService, OnlineHearingStateService onlineHearingStateService, QuestionRoundService questionRoundService,
                                 QuestionStateService questionStateService) {
        this.onlineHearingService = onlineHearingService;
        this.onlineHearingStateService = onlineHearingStateService;
        this.questionRoundService = questionRoundService;
        this.questionStateService = questionStateService;
    }

    @Override
    @Transactional
    public void execute(OnlineHearing onlineHearing) {
        Optional<OnlineHearingState> optState = onlineHearingStateService.retrieveOnlineHearingStateByState(OnlineHearingStates.QUESTIONS_ISSUED.getStateName());
        if (!optState.isPresent()) {
            log.debug("Unable to find online hearing state: " + OnlineHearingStates.QUESTIONS_ISSUED.getStateName());
            return;
        }

        Optional<QuestionState> optionalQuestionState = questionStateService.retrieveQuestionStateByStateName(QuestionStates.ISSUED.getStateName());
        if (!optionalQuestionState.isPresent()) {
            log.debug("Unable to find question state: " + QuestionStates.ISSUED.getStateName());
            return;
        }

        OnlineHearingState questionSentState = optState.get();

        // Having to do this because JPA is a pain
        Optional<OnlineHearing> optOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearing);
        if (optOnlineHearing.isPresent()) {
            OnlineHearing newOnlineHearing = optOnlineHearing.get();
            newOnlineHearing.setOnlineHearingState(questionSentState);
            newOnlineHearing.registerStateChange();
            onlineHearingService.updateOnlineHearing(newOnlineHearing);
        }

        List<Question> qrQuestions = questionRoundService.getQuestionsByQuestionRound(onlineHearing, questionRoundService.getCurrentQuestionRoundNumber(onlineHearing));
        questionRoundService.issueQuestionRound(optionalQuestionState.get(), qrQuestions);
        log.debug("QuestionSentTask.execute(). Online Hearing state update state updated to " + questionSentState.getState());
    }

    @Override
    public List<String> supports() {
        return Arrays.asList(EventTypes.QUESTION_ROUND_ISSUED.getEventType());
    }
}