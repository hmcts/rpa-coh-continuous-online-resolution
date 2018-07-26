package uk.gov.hmcts.reform.coh.controller.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;

public class EventRegistrationRequest {

    @NotNull(message = "Event Type Required")
    @JsonProperty("event_type")
    private String eventType;

    @NotNull(message = "Jurisdiction Required")
    @JsonProperty("jurisdiction")
    private String jurisdiction;

    @NotNull(message = "Endpoint Required")
    @JsonProperty("endpoint")
    private String endpoint;

    @PositiveOrZero @Max(5)
    @JsonProperty("maximum_retries")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer maxRetries = 3;

    @JsonProperty("active")
    @Pattern(regexp = "(?i:true|false)", message = "Active field must be true or false")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String active = "true";

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }


    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
}
