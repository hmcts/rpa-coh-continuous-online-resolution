package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateOnlinehearingResponse {

    @JsonProperty("online_hearing_id")
    private String onlinehearingId;

    public String getOnlinehearingId() {
        return onlinehearingId;
    }

    public void setOnlinehearingId(String onlinehearingId) {
        this.onlinehearingId = onlinehearingId;
    }
}
