package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CreateOnlineHearingResponse {

    @JsonProperty("online_hearing_id")
    private String onlineHearingId;

    @JsonProperty("state")
    private String state;

    @JsonProperty("case_id")
    private String caseId;

    @JsonProperty("panel")
    private List<OnlineHearingRequest.PanelMember> panel;

    @JsonProperty("jurisdiction")
    private String jurisdiction;

    public String getOnlineHearingId() {
        return onlineHearingId;
    }

    public void setOnlineHearingId(String onlineHearingId) {
        this.onlineHearingId = onlineHearingId;
    }

}
