package uk.gov.hmcts.reform.coh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
}
