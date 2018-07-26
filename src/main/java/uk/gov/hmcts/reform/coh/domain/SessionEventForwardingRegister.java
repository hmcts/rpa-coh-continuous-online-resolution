package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "session_event_forwarding_register")
public class SessionEventForwardingRegister {

    @EmbeddedId
    private SessionEventForwardingRegisterId id;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "event_type_id")
    @MapsId("eventTypeId")
    private SessionEventType sessionEventType;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "jurisdiction_id")
    @MapsId("jurisdictionId")
    private Jurisdiction jurisdiction;

    @Column(name = "forwarding_endpoint")
    private String forwardingEndpoint;

    @Column(name = "registration_date")
    private Date registrationDate;

    @Column(name = "maximum_retries")
    private Integer maximumRetries;

    @Column(name = "active")
    private Boolean active;

    public SessionEventForwardingRegister() {}

    public SessionEventForwardingRegister(Jurisdiction jurisdiction, SessionEventType sessionEventType,
                                          String forwardingEndpoint, Date registrationDate, Integer maximumRetries, Boolean active) {
        this.jurisdiction = jurisdiction;
        this.sessionEventType = sessionEventType;
        this.id = new SessionEventForwardingRegisterId(jurisdiction.getJurisdictionId(), sessionEventType.getEventTypeId());
        this.forwardingEndpoint = forwardingEndpoint;
        this.registrationDate = registrationDate;
        this.maximumRetries = maximumRetries;
        this.active = active;
    }

    public SessionEventForwardingRegisterId getEventForwardingRegisterId() {
        return id;
    }

    public void setEventForwardingRegisterId(SessionEventForwardingRegisterId sessionEventForwardingRegisterId) {
        this.id = sessionEventForwardingRegisterId;
    }

    public SessionEventType getSessionEventType() {
        return sessionEventType;
    }

    public void setSessionEventType(SessionEventType sessionEventType) {
        this.sessionEventType = sessionEventType;
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

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
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

    public static class Builder {
        private Jurisdiction jurisdiction;
        private SessionEventType sessionEventType;
        private String forwardingEndpoint;
        private Date registrationDate;
        private Integer maximumRetries;
        private Boolean active;

        public Builder jurisdiction(final Jurisdiction jurisdiction) {
            this.jurisdiction = jurisdiction;
            return this;
        }

        public Builder sessionEventType(final SessionEventType sessionEventType) {
            this.sessionEventType = sessionEventType;
            return this;
        }

        public Builder forwardingEndpoint(final String forwardingEndpoint) {
            this.forwardingEndpoint = forwardingEndpoint;
            return this;
        }

        public Builder registrationDate(final Date registrationDate) {
            this.registrationDate = registrationDate;
            return this;
        }

        public Builder maximumRetries(final Integer maximumRetries) {
            this.maximumRetries = maximumRetries;
            return this;
        }

        public Builder withActive(final Boolean active) {
            this.active = active;
            return this;
        }

        public SessionEventForwardingRegister build() {
            return new SessionEventForwardingRegister(
                    jurisdiction, sessionEventType, forwardingEndpoint,
                    registrationDate, maximumRetries, active
            );
        }
    }
}
