package uk.gov.hmcts.reform.coh.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.domain.RelistingState;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;
import uk.gov.hmcts.reform.coh.states.OnlineHearingStates;

import java.sql.Date;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Set online hearing state to RELISTED.
 */
@Component
public class RelistOnlineHearingTask implements ContinuousOnlineResolutionTask<OnlineHearing> {

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private OnlineHearingStateService onlineHearingStateService;

    @Autowired
    private Clock clock;

    @Override
    public void execute(OnlineHearing onlineHearing) {
        String relistedStateName = OnlineHearingStates.RELISTED.getStateName();
        Optional<OnlineHearingState> optionalOnlineHearingState
            = onlineHearingStateService.retrieveOnlineHearingStateByState(relistedStateName);

        optionalOnlineHearingState.ifPresent(onlineHearing::setOnlineHearingState);
        onlineHearing.setRelistState(RelistingState.ISSUED);
        onlineHearing.registerStateChange();
        onlineHearing.registerRelistingChange(Date.from(clock.instant()));
        onlineHearingService.updateOnlineHearing(onlineHearing);
    }

    @Override
    public List<String> supports() {
        return Collections.singletonList(EventTypes.ONLINE_HEARING_RELISTED.getEventType());
    }
}
