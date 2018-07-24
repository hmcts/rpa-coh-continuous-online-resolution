package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;

@Entity
@Table(name = "session_event_type")
public class SessionEventType {

    @Id
    @GeneratedValue
    @Column(name = "event_type_id")
    private int eventTypeId;

    @Column(name = "event_type_name")
    private String eventTypeName;

    public int getEventTypeId() {
        return eventTypeId;
    }

    public void setEventTypeId(int eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public String getEventTypeName() {
        return eventTypeName;
    }

    public void setEventTypeName(String eventTypeName) {
        this.eventTypeName = eventTypeName;
    }
}
