package uk.gov.hmcts.reform.coh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "online_hearing_state")
public class Onlinehearingstate {

    public static final int CREATED = 1;
    public static final int CLOSED = 2;

    @Id
    @Column(name = "online_hearing_state_id")
    @JsonIgnore
    private int onlinehearingStateId;

    @Column(name = "state")
    @JsonProperty("state_name")
    private String state;

    public Onlinehearingstate() {}

    public Onlinehearingstate(String state) {
        this.state = state;
    }

    public void setOnlinehearingStateId(int onlinehearingStateId) {
        this.onlinehearingStateId = onlinehearingStateId;
    }

    public int getOnlinehearingStateId() {
        return onlinehearingStateId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}