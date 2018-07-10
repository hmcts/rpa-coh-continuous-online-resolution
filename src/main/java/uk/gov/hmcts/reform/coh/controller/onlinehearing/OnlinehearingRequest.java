package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

public class OnlinehearingRequest {

    @JsonProperty("case_id")
    private String caseId;

    @JsonProperty("jurisdiction")
    private String jurisdiction;

    @JsonProperty("start_date")
    private Date startDate;

    @JsonProperty("panel")
    private List<PanelMember> panel;

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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public List<PanelMember> getPanel() {
        return panel;
    }

    public void setPanel(List<PanelMember> panel) {
        this.panel = panel;
    }

    public static class PanelMember {

        @JsonProperty("identity_token")
        private String identityToken;

        @JsonProperty("name")
        private String name;

        public String getIdentityToken() {
            return identityToken;
        }

        public void setIdentityToken(String identityToken) {
            this.identityToken = identityToken;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
