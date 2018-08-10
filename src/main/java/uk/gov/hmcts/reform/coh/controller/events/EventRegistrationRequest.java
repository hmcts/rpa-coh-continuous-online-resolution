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
        allowableValues = "question_round_issued, question_deadline_elapsed, question_deadline_extended, question_deadline_extension_denied, question_deadline_extension_granted, answers_submitted, decision_rejected, decision_issued")
    private String eventType;

    @NotNull(message = "Jurisdiction Required")
    @JsonProperty("jurisdiction")
    @ApiModelProperty(required = true)
    private String jurisdiction;

    @NotNull(message = "Endpoint Required")
    @JsonProperty("endpoint") @NotBlank @URL
    @ApiModelProperty(required = true)
    private String endpoint;

    @PositiveOrZero @Max(5)
    @JsonProperty("maximum_retries")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty(allowableValues = "0, 1, 2, 3, 4, 5", value = "3")
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
