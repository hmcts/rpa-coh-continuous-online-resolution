package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;

@Entity
@Table(name = "forwarding_state")
public class ForwardingState {

    @Id
    @GeneratedValue
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
