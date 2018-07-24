package uk.gov.hmcts.reform.coh.Notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.coh.domain.EventType;
import uk.gov.hmcts.reform.coh.events.EventTypes;

import java.util.Date;
import java.util.UUID;

public class NotificationRequest {

    @JsonProperty("case_id")
    private String caseId;

    @JsonProperty("online_hearing_id")
    private UUID onlineHearingId;

    @JsonProperty("event_type")
    private EventTypes eventType;

    @JsonProperty("expiry_date")
    private Date expiryDate;

    @JsonProperty("reason")
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

    @Override
    public String toString() {
        return "NotificationRequest{" +
                "caseId='" + caseId + '\'' +
                ", onlineHearingId=" + onlineHearingId +
                ", eventType='" + eventType + '\'' +
                '}';
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setEventType(EventTypes eventType) {
        this.eventType = eventType;
    }
}
