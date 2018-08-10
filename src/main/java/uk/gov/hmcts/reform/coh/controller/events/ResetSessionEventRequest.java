package uk.gov.hmcts.reform.coh.controller.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

public class ResetSessionEventRequest {

    @JsonProperty("jurisdiction")
    @NotNull(message = "Jurisdiction Required")
    @ApiModelProperty(required = true)
    private String jurisdiction;

    @JsonProperty("event_type")
    @NotNull(message = "Event type Required")
    @ApiModelProperty(required = true,
        allowableValues = "question_round_issued, question_deadline_elapsed, question_deadline_extended, question_deadline_extension_denied, question_deadline_extension_granted, answers_submitted, decision_rejected, decision_issued")
    private String eventType;

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
