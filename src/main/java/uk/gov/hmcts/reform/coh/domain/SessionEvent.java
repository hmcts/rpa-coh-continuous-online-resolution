package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "session_event")
public class SessionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "event_id")
    private UUID eventId;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "EVNT_ID", referencedColumnName = "event_type_id"),
            @JoinColumn(name = "JURS_ID", referencedColumnName = "jurisdiction_id") })
    private SessionEventForwardingRegister sessionEventForwardingRegister;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "online_hearing_id")
    private OnlineHearing onlineHearing;

    @Column(name = "retries")
    private int retries;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "forwarding_state_id")
    private SessionEventForwardingState sessionEventForwardingState;


    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public SessionEventForwardingRegister getSessionEventForwardingRegister() {
        return sessionEventForwardingRegister;
    }

    public void setSessionEventForwardingRegister(SessionEventForwardingRegister sessionEventForwardingRegister) {
        this.sessionEventForwardingRegister = sessionEventForwardingRegister;
    }

    public OnlineHearing getOnlineHearing() {
        return onlineHearing;
    }

    public void setOnlineHearing(OnlineHearing onlineHearing) {
        this.onlineHearing = onlineHearing;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public SessionEventForwardingState getSessionEventForwardingState() {
        return sessionEventForwardingState;
    }

    public void setSessionEventForwardingState(SessionEventForwardingState sessionEventForwardingState) {
        this.sessionEventForwardingState = sessionEventForwardingState;
    }
}
