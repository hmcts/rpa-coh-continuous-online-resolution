package uk.gov.hmcts.reform.coh.controller.answer;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

public class AnswerRequest {

    @ApiModelProperty(required = true)
    @JsonProperty("answer_text")
    private String answerText;

    @ApiModelProperty(required = true, allowableValues = "answer_drafted, answer_submitted")
    @JsonProperty("answer_state")
    private String answerState;

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public String getAnswerState() {
        return answerState;
    }

    public void setAnswerState(String answerState) {
        this.answerState = answerState;
    }
}
