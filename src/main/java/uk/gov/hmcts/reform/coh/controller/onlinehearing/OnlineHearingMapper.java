package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingStateHistory;

import java.util.Comparator;
import java.util.stream.Collectors;

public class OnlineHearingMapper {

    public static void map(OnlineHearingResponse response, OnlineHearing onlineHearing) {
        response.setOnlineHearingId(onlineHearing.getOnlineHearingId());
        response.setCaseId(onlineHearing.getCaseId());
        response.setStartDate(onlineHearing.getStartDate());
        response.setEndDate(onlineHearing.getEndDate());

        if (onlineHearing.getOnlineHearingStateHistories() != null && !onlineHearing.getOnlineHearingStateHistories().isEmpty()) {
            OnlineHearingStateHistory history =
            onlineHearing.getOnlineHearingStateHistories()
                    .stream()
                    .sorted(Comparator.comparing(OnlineHearingStateHistory::getDateOccurred).reversed())
                    .findFirst().get();
            ISO8601DateFormat df = new ISO8601DateFormat();
            response.setCurrentState(new OnlineHearingResponse.CurrentState(history.getOnlineHearingState().getState(),df.format(history.getDateOccurred())));
        }

        response.setPanel(onlineHearing.getPanelMembers()
                .stream()
                .map( p -> new OnlineHearingResponse.PanelMember(p.getFullName()))
                .collect(Collectors.toList()));
    }
}
