package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
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


//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//
//        if (o == null || getClass() != o.getClass())
//            return false;
//
//        OnlineHearingStateId that = (OnlineHearingStateId) o;
//        return Objects.equals(onlineHearingId, that.onlineHearingId) &&
//                Objects.equals(onlineHearingStateId, that.onlineHearingStateId);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(onlineHearingId, onlineHearingStateId);
//    }

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