package uk.gov.hmcts.reform.coh.controller.answer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.coh.controller.state.StateResponse;

import java.io.Serializable;
import java.util.List;

public class AnswerResponse implements Serializable {

    @JsonProperty("answer_id")
    private String answerId;

    @JsonProperty("answer_text")
    private String answerText;

    @JsonProperty("current_answer_state")
    private StateResponse stateResponse = new StateResponse();

    @JsonProperty(value = "history")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<StateResponse> histories;

    @JsonProperty(value = "uri")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String uri;

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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
