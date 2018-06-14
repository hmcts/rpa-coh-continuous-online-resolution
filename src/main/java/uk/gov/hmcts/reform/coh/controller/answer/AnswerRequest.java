package uk.gov.hmcts.reform.coh.controller.answer;

public class AnswerRequest {
    private String answerText;
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
