package uk.gov.hmcts.reform.coh.controller.question;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateQuestionRequest extends QuestionRequest {

    @JsonProperty("question_state")
    private String questionState;

    public String getQuestionState() {
        return questionState;
    }

    public void setQuestionState(String questionState) {
        this.questionState = questionState;
    }

}
