package uk.gov.hmcts.reform.coh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "answer_state")
public class AnswerState {

    @Id
    @Column(name = "answer_state_id")
    @JsonIgnore
    private int answerStateId;

    @Column(name = "state")
    @JsonProperty("state_name")
    private String state;

    public AnswerState() {}

    public AnswerState(int answerStateId, String state) {
        this.answerStateId = answerStateId;
        this.state = state;
    }

    public int getAnswerStateId() {
        return answerStateId;
    }

    public void setAnswerStateId(int answerStateId) {
        this.answerStateId = answerStateId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "AnswerState{" +
                "answerStateId=" + answerStateId +
                ", state='" + state + '\'' +
                '}';
    }
}