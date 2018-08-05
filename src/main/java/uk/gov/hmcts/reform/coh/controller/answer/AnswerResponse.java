package uk.gov.hmcts.reform.coh.controller.answer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.coh.controller.state.StateResponse;
import uk.gov.hmcts.reform.coh.domain.AnswerStateHistory;

import java.util.List;

public class AnswerResponse {

    @JsonProperty("answer_id")
    private String answerId;

    @JsonProperty("answer_text")
    private String answerText;

    @JsonProperty("current_answer_state")
    private StateResponse stateResponse = new StateResponse();

    @JsonProperty(value = "history")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<StateResponse> histories;

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

    public List<StateResponse> getHistories() {
        return histories;
    }

    public void setHistories(List<StateResponse> histories) {
        this.histories = histories;
    }
}
