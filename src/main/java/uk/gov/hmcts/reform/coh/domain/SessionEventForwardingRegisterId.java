package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class SessionEventForwardingRegisterId implements Serializable {

    @Column(name = "jurisdiction_id")
    private Long jurisdictionId;

    @Column(name = "event_type_id")
    private Integer eventTypeId;

    public SessionEventForwardingRegisterId() {
    }

    public SessionEventForwardingRegisterId(Long jurisdictionId, Integer eventTypeId) {
        this.jurisdictionId = jurisdictionId;
        this.eventTypeId = eventTypeId;
    }

    public Long getJurisdictionId() {
        return jurisdictionId;
    }

    public void setJurisdictionId(Long jurisdictionId) {
        this.jurisdictionId = jurisdictionId;
    }

    public Integer getEventTypeId() {
        return eventTypeId;
    }

    public void setEventTypeId(Integer eventTypeId) {
        this.eventTypeId = eventTypeId;
    }
}
