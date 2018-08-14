package uk.gov.hmcts.reform.coh.controller.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

public class EventRegistrationRequest {

    @NotNull(message = "Event Type Required")
    @JsonProperty("event_type")
    @ApiModelProperty(required = true,
            allowableValues = "question_round_issued,question_deadline_elapsed,question_deadline_extended,question_deadline_extension_denied,question_deadline_extension_granted,answers_submitted, decision_rejected, decision_issued",
            value = "The event type"
    )
    private String eventType;

    @NotNull(message = "Jurisdiction Required")
    @JsonProperty("jurisdiction")
    @ApiModelProperty(required = true, allowableValues = "SSCS", value = "The Jurisdiction interested in the event")
    private String jurisdiction;

    @NotNull(message = "Endpoint Required")
    @JsonProperty("endpoint") @NotBlank @URL
    @ApiModelProperty(required = true, value = "An absolute URL corresponding to the callback endpoint for the event")
    private String endpoint;

    @PositiveOrZero @Max(5)
    @JsonProperty("maximum_retries")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty(allowableValues = "range[0,5]", value = "The number of retry attempt to send a notification message. Default 3")
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
