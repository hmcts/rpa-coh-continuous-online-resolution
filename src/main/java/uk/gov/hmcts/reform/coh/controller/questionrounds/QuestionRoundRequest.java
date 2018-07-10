package uk.gov.hmcts.reform.coh.controller.questionrounds;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuestionRoundRequest {

    @JsonProperty("question_round_number")
    private String questionRoundNumber;

    @JsonProperty("state_name")
    private String stateName;

    public String getQuestionRoundNumber() {
        return questionRoundNumber;
    }

    public void setQuestionRoundNumber(String questionRoundNumber) {
        this.questionRoundNumber = questionRoundNumber;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }
}
