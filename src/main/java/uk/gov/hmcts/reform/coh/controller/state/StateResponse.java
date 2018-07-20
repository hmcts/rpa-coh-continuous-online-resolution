package uk.gov.hmcts.reform.coh.controller.state;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StateResponse {

    @JsonProperty(value = "state_name")
    private String name;

    @JsonProperty(value = "state_datetime")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String datetime;

    public StateResponse() {
    }

    public StateResponse(String name, String datetime) {
        this.name = name;
        this.datetime = datetime;
    }

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
