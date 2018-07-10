package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class OnlineHearingStateId implements Serializable {

    @Column(name = "online_hearing_id")
    private UUID onlineHearingId;

    @Column(name = "online_hearing_state_id")
    private int onlineHearingStateId;


    public OnlineHearingStateId(){}

    public OnlineHearingStateId(UUID onlineHearingId, int onlineHearingStateId){
        this.onlineHearingId = onlineHearingId;
        this.onlineHearingStateId = onlineHearingStateId;
    }

    public UUID getOnlineHearingId() {
        return onlineHearingId;
    }

    public void setOnlineHearingId(UUID onlineHearingId) {
        this.onlineHearingId = onlineHearingId;
    }

    public int getOnlineHearingStateId() {
        return onlineHearingStateId;
    }

    public void setOnlineHearingStateId(int onlineHearingStateId) {
        this.onlineHearingStateId = onlineHearingStateId;
    }
}