package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "event_forwarding_register")
public class EventForwardingRegister {

    @Id
    @GeneratedValue
    @Column(name = "event_forwarding_register_id")
    private int eventForwardingRegisterId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "event_type_id")
    private EventType eventType;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "jurisdiction_id")
    private Jurisdiction jurisdiction;

    @Column(name = "forwarding_endpoint")
    private String forwardingEndpoint;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "registration_date")
    private Date registrationDate = new Date();

    @Column(name = "maximum_retries")
    private Integer maximumRetries;

    @Column(name = "active")
    private Boolean active;

    public int getEventForwardingRegisterId() {
        return eventForwardingRegisterId;
    }

    public void setEventForwardingRegisterId(int eventForwardingRegisterId) {
        this.eventForwardingRegisterId = eventForwardingRegisterId;
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

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }
}
