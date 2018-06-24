package uk.gov.hmcts.reform.coh.controller.answer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnswerRequest {

    @JsonProperty("answer_text")
    private String answerText;

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
