package uk.gov.hmcts.reform.coh.controller.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.coh.domain.QuestionState;

public class QuestionResponse extends QuestionRequest {

    @JsonProperty(value = "question_id")
    private String questionId;

    @JsonProperty(value = "question_state")
    private QuestionState questionState = new QuestionState();

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public QuestionState getQuestionState() {
        return questionState;
    }

    public void setQuestionState(QuestionState questionState) {
        this.questionState = questionState;
    }
}
