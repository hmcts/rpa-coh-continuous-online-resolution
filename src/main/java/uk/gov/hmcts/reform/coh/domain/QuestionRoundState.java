package uk.gov.hmcts.reform.coh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class QuestionRoundState {

    @JsonProperty("state_name")
    private String state;

    @JsonIgnore
    private int stateId;

    public void setState(QuestionState questionState){
        this.state = questionState.getState();
        this.stateId = questionState.getQuestionStateId();
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionRoundState that = (QuestionRoundState) o;
        return stateId == that.stateId &&
                Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {

        return Objects.hash(state, stateId);
    }

    @Override
    public String toString() {
        return "QuestionRoundState{" +
                "state='" + state + '\'' +
                ", stateId=" + stateId +
                '}';
    }
}
