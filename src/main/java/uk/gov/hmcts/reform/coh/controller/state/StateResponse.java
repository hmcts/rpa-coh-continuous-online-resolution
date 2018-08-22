package uk.gov.hmcts.reform.coh.controller.state;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.text.WordUtils;

import java.io.Serializable;

public class StateResponse implements Serializable {

    @JsonProperty(value = "state_name")
    private String name;

    @JsonProperty(value = "state_desc")
    private String stateDesc;

    @JsonProperty(value = "state_datetime")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String datetime;

    public StateResponse() {
    }

    public StateResponse(String name, String datetime) {
        this.name = name;
        this.datetime = datetime;
        this.stateDesc = getStateDesc();
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

    public String getStateDesc() {
        return WordUtils.capitalize(name.replaceAll("_", " "));
    }
}
