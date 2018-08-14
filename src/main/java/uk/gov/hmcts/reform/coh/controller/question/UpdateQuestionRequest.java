package uk.gov.hmcts.reform.coh.controller.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

public class UpdateQuestionRequest extends QuestionRequest {

    @JsonProperty("question_state")
    @ApiModelProperty(required = true, allowableValues = "question_drafted")
    private String questionState;

    public String getQuestionState() {
        return questionState;
    }

    public void setQuestionState(String questionState) {
        this.questionState = questionState;
    }

}
