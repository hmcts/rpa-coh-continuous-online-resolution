package uk.gov.hmcts.reform.coh.controller.answer;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.coh.controller.state.StateResponse;

public class AnswerResponse {

    @JsonProperty("answer_id")
    private String answerId;

    @JsonProperty("answer_text")
    private String answerText;

    @JsonProperty("current_answer_state")
    private StateResponse stateResponse;

    public String getAnswerId() {
        return answerId;
    }

    public void setAnswerId(String answerId) {
        this.answerId = answerId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public StateResponse getStateResponse() {
        return stateResponse;
    }

    public void setStateResponse(StateResponse stateResponse) {
        this.stateResponse = stateResponse;
    }
}
