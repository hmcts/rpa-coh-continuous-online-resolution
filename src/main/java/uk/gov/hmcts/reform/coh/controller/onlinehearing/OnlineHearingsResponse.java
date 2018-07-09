package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class OnlineHearingsResponse {

    @JsonProperty(value = "online_hearings")
    private List<OnlineHearingResponse> onlineHearingResponses = new ArrayList<>();

    public List<OnlineHearingResponse> getOnlineHearingResponses() {
        return onlineHearingResponses;
    }

    public void setOnlineHearingResponses(List<OnlineHearingResponse> onlineHearingResponses) {
        this.onlineHearingResponses = onlineHearingResponses;
    }
}
