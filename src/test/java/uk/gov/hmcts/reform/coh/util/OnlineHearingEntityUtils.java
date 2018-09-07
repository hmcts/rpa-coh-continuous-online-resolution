package uk.gov.hmcts.reform.coh.util;

import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingPanelMember;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingStateHistory;
import uk.gov.hmcts.reform.coh.states.OnlineHearingStates;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

public class OnlineHearingEntityUtils {

    public static final OnlineHearing createTestOnlineHearingEntity() {
        return createTestOnlineHearingEntity(OnlineHearingStates.STARTED);
    }

    public static final OnlineHearing createTestOnlineHearingEntity(OnlineHearingStates state) {

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(UUID.randomUUID());
        onlineHearing.setCaseId("case_123");
        onlineHearing.setStartDate(new Date());
        onlineHearing.setOnlineHearingState(OnlineHearingStateUtils.get(state));
        OnlineHearingStateHistory history1 = new OnlineHearingStateHistory();

        history1.setDateOccurred(new Date());
        history1.setOnlinehearingstate(onlineHearing.getOnlineHearingState());
        onlineHearing.setOnlineHearingStateHistories(Arrays.asList(history1));

        OnlineHearingPanelMember member = new OnlineHearingPanelMember();
        member.setFullName("full name");
        member.setRole("judge");
        onlineHearing.setPanelMembers(Arrays.asList(member));

        return onlineHearing;
    }
}
