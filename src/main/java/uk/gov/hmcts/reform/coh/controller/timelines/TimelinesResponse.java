package uk.gov.hmcts.reform.coh.controller.timelines;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingResponse;

public class TimelinesResponse {

    @JsonProperty(value = "online_hearing")
    private OnlineHearingResponse onlineHearing;

    public OnlineHearingResponse getOnlineHearing() {
        return onlineHearing;
    }

    public void setOnlineHearing(OnlineHearingResponse onlineHearing) {
        this.onlineHearing = onlineHearing;
    }
}
