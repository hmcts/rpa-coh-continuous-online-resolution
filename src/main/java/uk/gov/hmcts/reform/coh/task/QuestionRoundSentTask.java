package uk.gov.hmcts.reform.coh.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingStates;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.service.NotificationService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;

import java.util.Optional;

@Service
public class QuestionRoundSentTask implements ContinuousOnlineResolutionTask<OnlineHearing> {

    private static final Logger log = LoggerFactory.getLogger(QuestionRoundSentTask.class);

    private OnlineHearingService onlineHearingService;

    private OnlineHearingStateService onlineHearingStateService;

    private NotificationService notificationService;

    @Autowired
    public QuestionRoundSentTask(OnlineHearingService onlineHearingService, OnlineHearingStateService onlineHearingStateService,
                                 NotificationService notificationService) {
        this.onlineHearingService = onlineHearingService;
        this.onlineHearingStateService = onlineHearingStateService;
        this.notificationService = notificationService;
    }

    @Override
    public void execute(OnlineHearing onlineHearing) {

        Optional<OnlineHearingState> optState = onlineHearingStateService.retrieveOnlineHearingStateByState(OnlineHearingStates.QUESTIONS_ISSUED.getStateName());
        if (!optState.isPresent()) {
            log.debug("Unable to find online hearing state: " + OnlineHearingStates.QUESTIONS_ISSUED.getStateName());
            return;
        }

        OnlineHearingState questionSentState = optState.get();
        onlineHearing.setOnlineHearingState(questionSentState);
        onlineHearing.registerStateChange();
        onlineHearingService.updateOnlineHearing(onlineHearing);

        log.debug("QuestionSentTask.execute(). Online Hearing state update state updated to " + questionSentState.getState());

        notificationService.notifyIssuedQuestionRound(onlineHearing);
    }
}
