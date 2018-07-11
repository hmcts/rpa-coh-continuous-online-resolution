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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public List<OnlineHearingRequest.PanelMember> getPanel() {
        return panel;
    }

    public void setPanel(List<OnlineHearingRequest.PanelMember> panel) {
        this.panel = panel;
    }
}
