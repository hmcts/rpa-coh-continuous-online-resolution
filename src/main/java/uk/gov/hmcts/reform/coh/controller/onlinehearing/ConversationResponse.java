package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConversationResponse {

    @JsonProperty(value = "online_hearing")
    private OnlineHearingResponse onlineHearing;

    public OnlineHearingResponse getOnlineHearing() {
        return onlineHearing;
    }

    public void setOnlineHearing(OnlineHearingResponse onlineHearing) {
        this.onlineHearing = onlineHearing;
    }
}
