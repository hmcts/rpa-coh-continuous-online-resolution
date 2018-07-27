package uk.gov.hmcts.reform.coh.schedule.notifiers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.coh.events.EventTypes;

import java.util.Date;
import java.util.UUID;

public class NotificationRequest {

    @JsonProperty("case_id")
    private String caseId;

    @JsonProperty("online_hearing_id")
    private UUID onlineHearingId;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("expiry_date")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String expiryDate;

    @JsonProperty("reason")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String reason;

    public NotificationRequest() {
    }

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

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "NotificationRequest{" +
                "caseId='" + caseId + '\'' +
                ", onlineHearingId=" + onlineHearingId +
                ", eventType='" + eventType + '\'' +
                (expiryDate == null ? "" : ", expiryDate=" + expiryDate) +
                (reason == null ? "" : ", reason=" + reason) +
                '}';
    }
}
