package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class OnlinehearingStateId implements Serializable {

    @Column(name = "online_hearing_id")
    private UUID onlinehearingId;

    @Column(name = "online_hearing_state_id")
    private int onlinehearingStateId;


    public OnlinehearingStateId(){}

    public OnlinehearingStateId(UUID onlinehearingId, int onlinehearingStateId){
        this.onlinehearingId = onlinehearingId;
        this.onlinehearingStateId = onlinehearingStateId;
    }

    public UUID getOnlinehearingId() {
        return onlinehearingId;
    }

    public void setOnlinehearingId(UUID onlinehearingId) {
        this.onlinehearingId = onlinehearingId;
    }

    public int getOnlinehearingStateId() {
        return onlinehearingStateId;
    }

    public void setOnlinehearingStateId(int onlinehearingStateId) {
        this.onlinehearingStateId = onlinehearingStateId;
    }
}