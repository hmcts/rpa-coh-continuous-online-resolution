package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;

@Entity
@Table(name = "event_forwarding_register")
public class EventForwardingRegister {

    @EmbeddedId
    private EventForwardingRegisterId id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "event_type_id")
    @MapsId("eventTypeId")
    private EventType eventType;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "jurisdiction_id")
    @MapsId("jurisdictionId")
    private Jurisdiction jurisdiction;

    @Column(name = "forwarding_endpoint")
    private String forwardingEndpoint;

    @Column(name = "registration_date")
    private String registrationDate;

    @Column(name = "maximum_retries")
    private Integer maximumRetries;

    @Column(name = "active")
    private Boolean active;

    public EventForwardingRegister(Jurisdiction jurisdiction,
                                   EventType eventType) {
        this.jurisdiction = jurisdiction;
        this.eventType = eventType;
        this.id = new EventForwardingRegisterId(jurisdiction.getJurisdictionId(), eventType.getEventTypeId());
    }


    public EventForwardingRegisterId getEventForwardingRegisterId() {
        return id;
    }

    public void setEventForwardingRegisterId(EventForwardingRegisterId eventForwardingRegisterId) {
        this.id = eventForwardingRegisterId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Jurisdiction getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(Jurisdiction jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public String getForwardingEndpoint() {
        return forwardingEndpoint;
    }

    public void setForwardingEndpoint(String forwardingEndpoint) {
        this.forwardingEndpoint = forwardingEndpoint;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Integer getMaximumRetries() {
        return maximumRetries;
    }

    public void setMaximumRetries(Integer maximumRetries) {
        this.maximumRetries = maximumRetries;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
