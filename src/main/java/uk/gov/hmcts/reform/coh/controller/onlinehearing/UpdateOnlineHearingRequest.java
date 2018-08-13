package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

public class UpdateOnlineHearingRequest {

    @JsonProperty(value = "online_hearing_state")
    @ApiModelProperty(required = true, allowableValues = "continuous_online_hearing_relisted")
    private String state;

    @JsonProperty(value = "reason")
    private String reason;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
