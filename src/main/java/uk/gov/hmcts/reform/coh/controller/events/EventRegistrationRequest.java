package uk.gov.hmcts.reform.coh.controller.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

public class EventRegistrationRequest {

    @NotNull(message = "Event Type Required")
    @JsonProperty("event_type")
    private String eventType;

    @NotNull(message = "Jurisdiction Required")
    @JsonProperty("jurisdiction")
    private String jurisdiction;

    @NotNull(message = "Endpoint Required")
    @JsonProperty("endpoint") @NotBlank @URL
    private String endpoint;

    @PositiveOrZero @Max(5)
    @JsonProperty("maximum_retries")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer maxRetries = 3;


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

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
}
