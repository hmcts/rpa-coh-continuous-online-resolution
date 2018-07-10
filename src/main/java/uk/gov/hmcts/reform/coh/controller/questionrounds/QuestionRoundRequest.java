package uk.gov.hmcts.reform.coh.controller.questionrounds;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuestionRoundRequest {

    @JsonProperty("state_name")
    private String stateName;

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }
}
