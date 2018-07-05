package uk.gov.hmcts.reform.coh.controller.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.coh.controller.state.StateResponse;
import uk.gov.hmcts.reform.coh.domain.QuestionState;

public class QuestionResponse extends QuestionRequest {

    @JsonProperty(value = "question_id")
    private String questionId;

    @JsonProperty(value = "current_question_state")
    private StateResponse currentState = new StateResponse();

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public StateResponse getCurrentState() {
        return currentState;
    }

    public void setCurrentState(StateResponse currentState) {
        this.currentState = currentState;
    }
}
