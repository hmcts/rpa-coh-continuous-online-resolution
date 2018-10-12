package uk.gov.hmcts.reform.coh.task;

import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingStateHistory;
import uk.gov.hmcts.reform.coh.domain.RelistingHistory;
import uk.gov.hmcts.reform.coh.domain.RelistingState;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;
import uk.gov.hmcts.reform.coh.states.OnlineHearingStates;
import uk.gov.hmcts.reform.coh.util.OnlineHearingEntityUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class RelistOnlineHearingTaskTest {

    private OnlineHearing onlineHearing;

    @Spy
    private OnlineHearingState relistedState;

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private OnlineHearingStateService onlineHearingStateService;

    @Mock
    private Clock clock;

    @InjectMocks
    private RelistOnlineHearingTask relistOnlineHearingTask;

    private String onlineHearingRelistedState;

    @Before
    public void setup() {
        onlineHearingRelistedState = OnlineHearingStates.RELISTED.getStateName();
        onlineHearing = OnlineHearingEntityUtils.createTestOnlineHearingEntity();
        when(relistedState.getState()).thenReturn(onlineHearingRelistedState);
        when(onlineHearingStateService.retrieveOnlineHearingStateByState(onlineHearingRelistedState))
            .thenReturn(Optional.of(relistedState));

        when(clock.instant()).thenReturn(Instant.now());
    }

    @Test
    public void supportsOnlyRelistingEvent() {
        assertThat(relistOnlineHearingTask.supports()).containsOnly(EventTypes.ONLINE_HEARING_RELISTED.getEventType());
    }

    @Test
    public void changesStateToRelisted() {
        relistOnlineHearingTask.execute(onlineHearing);

        assertThat(onlineHearing.getOnlineHearingState().getState()).isEqualTo(onlineHearingRelistedState);
    }

    @Test
    public void changesRelistingStateToIssued() {
        relistOnlineHearingTask.execute(onlineHearing);

        assertThat(onlineHearing.getRelistState()).isEqualTo(RelistingState.ISSUED);
    }

    @Test
    public void storesUpdatedOnlineHearing() {
        relistOnlineHearingTask.execute(onlineHearing);

        verify(onlineHearingService, atLeastOnce()).updateOnlineHearing(onlineHearing);
    }

    @Test
    public void registersStateChange() {
        relistOnlineHearingTask.execute(onlineHearing);

        Condition<OnlineHearingStateHistory> relisted = new Condition<>(
            history -> onlineHearingRelistedState.equals(history.getOnlinehearingstate().getState()),
            "relisted"
        );

        assertThat(onlineHearing.getOnlineHearingStateHistories()).areExactly(1, relisted);
    }

    @Test
    public void registersRelistingStateChange() {
        relistOnlineHearingTask.execute(onlineHearing);

        Condition<RelistingHistory> relisted = new Condition<>(
            history -> RelistingState.ISSUED.equals(history.getRelistState()),
            "relisted"
        );

        assertThat(onlineHearing.getRelistingHistories()).areExactly(1, relisted);
    }
}
