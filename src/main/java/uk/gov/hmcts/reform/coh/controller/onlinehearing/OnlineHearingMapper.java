package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import uk.gov.hmcts.reform.coh.controller.state.StateResponse;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;

import java.util.stream.Collectors;

public class OnlineHearingMapper {
    private static final ISO8601DateFormat df = new ISO8601DateFormat();

    public static void map(OnlineHearingResponse response, OnlineHearing onlineHearing) {
        response.setOnlineHearingId(onlineHearing.getOnlineHearingId());
        response.setCaseId(onlineHearing.getCaseId());
        response.setStartDate(onlineHearing.getStartDate());
        response.setEndDate(onlineHearing.getEndDate());
        response.setPanel(onlineHearing.getPanelMembers()
                .stream()
                .map( p -> new OnlineHearingResponse.PanelMember(p.getFullName()))
                .collect(Collectors.toList()));
        response.getCurrentState().setName(onlineHearing.getOnlineHearingState().getState());

        if (onlineHearing.getOnlineHearingStateHistories() != null && !onlineHearing.getOnlineHearingStateHistories().isEmpty()){
            response.getCurrentState().setDatetime
                    (df.format(onlineHearing.getOnlineHearingStateHistories().stream().sorted(
                            (a, b) -> (b.getDateOccurred().compareTo(a.getDateOccurred()))).collect(Collectors.toList()
                    ).get(onlineHearing.getOnlineHearingStateHistories().size()-1).getDateOccurred()));
        }

        if (onlineHearing.getOnlineHearingStateHistories() != null && !onlineHearing.getOnlineHearingStateHistories().isEmpty()) {
            response.setHistories(onlineHearing
                    .getOnlineHearingStateHistories()
                    .stream()
                    .map(h -> {
                        return new StateResponse(h.getOnlinehearingstate().getState(), df.format(h.getDateOccurred()));
                    })
                    .collect(Collectors.toList()));
        }
    }
}
