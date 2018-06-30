package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import uk.gov.hmcts.reform.coh.domain.OnlineHearing;

import java.util.stream.Collectors;

public class OnlineHearingMapper {

    private OnlineHearingResponse response;
    private OnlineHearing onlineHearing;

    public OnlineHearingMapper(OnlineHearingResponse response, OnlineHearing onlineHearing) {
        this.response = response;
        this.onlineHearing = onlineHearing;
    }

    public void map() {
        response.setOnlineHearingId(onlineHearing.getOnlineHearingId());
        response.setCaseId(onlineHearing.getExternalRef());
        response.setStartDate(onlineHearing.getStartDate());
        response.setEndDate(onlineHearing.getEndDate());
        response.setPanel(onlineHearing.getPanelMembers()
                .stream()
                .map( p -> new OnlineHearingResponse.PanelMember(p.getFullName()))
                .collect(Collectors.toList()));
    }
}
