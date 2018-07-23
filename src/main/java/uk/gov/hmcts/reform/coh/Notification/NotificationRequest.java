package uk.gov.hmcts.reform.coh.Notification;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class NotificationRequest {

    @JsonProperty("case_id")
    private String caseId;

    @JsonProperty("online_hearing_id")
    private UUID onlineHearingId;

    @JsonProperty("event_type")
    private String eventType;

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public UUID getOnlineHearingId() {
        return onlineHearingId;
    }

    public void setOnlineHearingId(UUID onlineHearingId) {
        this.onlineHearingId = onlineHearingId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "NotificationRequest{" +
                "caseId='" + caseId + '\'' +
                ", onlineHearingId=" + onlineHearingId +
                ", eventType='" + eventType + '\'' +
                '}';
    }
}
