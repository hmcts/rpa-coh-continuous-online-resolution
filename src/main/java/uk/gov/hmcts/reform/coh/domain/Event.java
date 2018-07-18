package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;

@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue
    @Column(name = "event_id")
    private int eventId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "event_forwarding_register_id")
    private EventForwardingRegister eventForwardingRegister;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "online_hearing_id")
    private OnlineHearing onlineHearing;

    @Column(name = "retries")
    private Integer retries;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forwarding_state_id")
    private ForwardingState forwardingState;

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
}
