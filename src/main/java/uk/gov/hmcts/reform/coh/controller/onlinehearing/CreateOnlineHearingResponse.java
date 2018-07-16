package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CreateOnlineHearingResponse {

    @JsonProperty("online_hearing_id")
    private String onlineHearingId;

    public String getOnlineHearingId() {
        return onlineHearingId;
    }

    public void setOnlineHearingId(String onlineHearingId) {
        this.onlineHearingId = onlineHearingId;
    }

}
