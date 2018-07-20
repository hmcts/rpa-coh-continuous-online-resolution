package uk.gov.hmcts.reform.coh.task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingStates;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;

import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class QuestionRoundSentTaskTest {

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private OnlineHearingStateService onlineHearingStateService;

    @InjectMocks
    private QuestionRoundSentTask questionRoundSentTask;

    private OnlineHearing onlineHearing;

    private OnlineHearingState onlineHearingState;

    @Before
    public void setup() {
        String statename = OnlineHearingStates.QUESTIONS_ISSUED.getStateName();
        onlineHearing = new OnlineHearing();
        onlineHearingState = new OnlineHearingState();
        onlineHearingState.setState(statename);
        given(onlineHearingService.updateOnlineHearing(onlineHearing)).willReturn(onlineHearing);
        given(onlineHearingStateService.retrieveOnlineHearingStateByState(statename)).willReturn(java.util.Optional.ofNullable(onlineHearingState));
    }

    @Test
    public void testSave() {
        questionRoundSentTask.execute(onlineHearing);
    }
}
