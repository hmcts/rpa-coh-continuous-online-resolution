package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;

@Entity
@Table(name = "session_event_forwarding_state")
public class SessionEventForwardingState {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "forwarding_state_id")
    private int forwardingStateId;

    @Column(name = "forwarding_state_name")
    private String forwardingStateName;

    public int getForwardingStateId() {
        return forwardingStateId;
    }

    public void setForwardingStateId(int forwardingStateId) {
        this.forwardingStateId = forwardingStateId;
    }

    public String getForwardingStateName() {
        return forwardingStateName;
    }

    public void setForwardingStateName(String forwardingStateName) {
        this.forwardingStateName = forwardingStateName;
    }
}
