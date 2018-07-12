package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import uk.gov.hmcts.reform.coh.domain.OnlineHearing;

import java.util.stream.Collectors;

public class OnlineHearingMapper {

    public static void map(OnlineHearingResponse response, OnlineHearing onlineHearing) {
        response.setOnlineHearingId(onlineHearing.getOnlineHearingId());
        response.setCaseId(onlineHearing.getCaseId());
        response.setStartDate(onlineHearing.getStartDate());
        response.setEndDate(onlineHearing.getEndDate());
        response.setState(onlineHearing.getOnlineHearingState().getState());
        response.setPanel(onlineHearing.getPanelMembers()
                .stream()
                .map( p -> new OnlineHearingResponse.PanelMember(p.getFullName()))
                .collect(Collectors.toList()));
    }
}
