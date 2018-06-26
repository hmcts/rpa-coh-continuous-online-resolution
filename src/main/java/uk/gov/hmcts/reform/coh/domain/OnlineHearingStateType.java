package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "Online Hearing State Type")
@Table(name = "online_hearing_state_type")
public class OnlineHearingStateType {

    public static final int CREATED = 1;
    public static final int CLOSED = 2;

    @Id
    @Column(name = "online_hearing_state_id")
    private int onlineHearingStateId;

    @Column(name = "online_hearing_state")
    private String state;

    public OnlineHearingStateType(String state) {
        this.state = state;
    }

    public int getOnlineHearingStateId() {
        return onlineHearingStateId;
    }

    public void setOnlineHearingStateId(int onlineHearingStateId) {
        this.onlineHearingStateId = onlineHearingStateId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
