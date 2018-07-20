package uk.gov.hmcts.reform.coh.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingStates;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;

import java.util.Optional;

@Service
@Component
public class QuestionSentTask implements ContinuousOnlineResolutionTask<OnlineHearing> {

    private static final Logger log = LoggerFactory.getLogger(QuestionSentTask.class);

    private OnlineHearingService onlineHearingService;

    private OnlineHearingStateService onlineHearingStateService;

    private OnlineHearingState questionSentState;

    @Autowired
    public QuestionSentTask(OnlineHearingService onlineHearingService, OnlineHearingStateService onlineHearingStateService) {
        this.onlineHearingService = onlineHearingService;
        this.onlineHearingStateService = onlineHearingStateService;

        Optional<OnlineHearingState> optState = onlineHearingStateService.retrieveOnlineHearingStateByState(OnlineHearingStates.QUESTIONS_ISSUED.getStateName());
        if (optState.isPresent()) {
            questionSentState = optState.get();
        }
    }

    @Override
    public void execute(OnlineHearing onlineHearing) {

        onlineHearing.setOnlineHearingState(questionSentState);
        onlineHearing.registerStateChange();
        onlineHearingService.updateOnlineHearing(onlineHearing);

        log.debug("QuestionSentTask.execute(). Online Hearing state update state updated to " + questionSentState.getState());
    }
}
