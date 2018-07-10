package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import uk.gov.hmcts.reform.coh.domain.Onlinehearing;

import java.util.stream.Collectors;

public class OnlinehearingMapper {

    private OnlinehearingResponse response;
    private Onlinehearing onlinehearing;

    public OnlinehearingMapper(OnlinehearingResponse response, Onlinehearing onlinehearing) {
        this.response = response;
        this.onlinehearing = onlinehearing;
    }

    public void map() {
        response.setOnlinehearingId(onlinehearing.getOnlinehearingId());
        response.setCaseId(onlinehearing.getCaseId());
        response.setStartDate(onlinehearing.getStartDate());
        response.setEndDate(onlinehearing.getEndDate());
        response.setPanel(onlinehearing.getPanelMembers()
                .stream()
                .map( p -> new OnlinehearingResponse.PanelMember(p.getFullName()))
                .collect(Collectors.toList()));
    }
}
