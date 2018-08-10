package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;

public class OnlineHearingRequest {

    @JsonProperty("case_id")
    @ApiModelProperty(required = true)
    private String caseId;

    @JsonProperty("jurisdiction")
    @ApiModelProperty(required = true)
    private String jurisdiction;

    @JsonProperty("start_date")
    @ApiModelProperty(required = true)
    private Date startDate;

    @JsonProperty("panel")
    @ApiModelProperty(required = true)
    private List<PanelMember> panel;

    @JsonProperty("state")
    private String state;

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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public static class PanelMember {

        @ApiModelProperty(required = true)
        @JsonProperty("identity_token")
        private String identityToken;

        @ApiModelProperty(required = true)
        @JsonProperty("name")
        private String name;

        @JsonProperty("role")
        private String role;

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

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
