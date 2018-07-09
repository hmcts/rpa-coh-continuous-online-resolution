package uk.gov.hmcts.reform.coh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "decision_state")
public class DecisionState {

    @Id
    @Column(name = "decision_state_id")
    @JsonIgnore
    private int decisionStateId;

    @Column(name = "state")
    @JsonProperty("state_name")
    private String state;

    public int getDecisionStateId() {
        return decisionStateId;
    }

    public void setDecisionStateId(int decisionStateId) {
        this.decisionStateId = decisionStateId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}