package uk.gov.hmcts.reform.coh.controller.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class ResetSessionEventRequest {

    @JsonProperty("jurisdiction")
    @NotNull(message = "Jurisdiction Required")
    private String jurisdiction;

    @JsonProperty("event_type")
    @NotNull(message = "Event type Required")
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
