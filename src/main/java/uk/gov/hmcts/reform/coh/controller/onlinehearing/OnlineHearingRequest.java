package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OnlineHearingRequest {

    @JsonProperty("case_id")
    @ApiModelProperty(required = true)
    private String caseId;

    @JsonProperty("jurisdiction")
    @ApiModelProperty(required = true, allowableValues = "SSCS")
    private String jurisdiction;

    @JsonProperty("start_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @ApiModelProperty(required = true, example = "2018-12-12T12:23:26Z", value = "The start date of the online resolution")
    private Date startDate;

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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
