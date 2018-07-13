package uk.gov.hmcts.reform.coh.controller.state;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StateResponse {

    @JsonProperty(value = "state_name")
    private String name;

    @JsonProperty(value = "state_datetime")
    private String datetime;

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
