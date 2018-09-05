package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "online_hearing_state")
public class OnlineHearingState {

    @Id
    @Column(name = "online_hearing_state_id")
    private int onlineHearingStateId;

    @Column(name = "state")
    private String state;

    public OnlineHearingState() {}

    public OnlineHearingState(String state) {
        this.state = state;
    }

    public OnlineHearingState(int onlineHearingStateId, String state) {
        this.onlineHearingStateId = onlineHearingStateId;
        this.state = state;
    }

    public void setOnlineHearingStateId(int onlineHearingStateId) {
        this.onlineHearingStateId = onlineHearingStateId;
    }

    public int getOnlineHearingStateId() {
        return onlineHearingStateId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}