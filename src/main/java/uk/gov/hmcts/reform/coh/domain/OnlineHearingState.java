package uk.gov.hmcts.reform.coh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "online_hearing_state")
public class OnlineHearingState {

    public static final int CREATED = 1;
    public static final int CLOSED = 2;

    @Id
    @Column(name = "online_hearing_state_id")
    @JsonIgnore
    private int onlineHearingStateId;

    @Column(name = "state")
    private String state;

    public OnlineHearingState() {}

    public OnlineHearingState(String state) {
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