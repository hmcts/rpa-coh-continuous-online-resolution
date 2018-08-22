package uk.gov.hmcts.reform.coh.controller.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.controller.state.StateResponse;
import uk.gov.hmcts.reform.coh.controller.utils.CohISO8601DateFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class QuestionResponse extends QuestionRequest implements Serializable {

    @JsonProperty(value = "question_id")
    private String questionId;

    @JsonProperty(value = "deadline_expiry_date")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String deadlineExpiryDate;

    @JsonProperty(value = "current_question_state")
    private StateResponse currentState = new StateResponse();

    @JsonProperty(value = "answers")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<AnswerResponse> answers;

    @JsonProperty(value = "history")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<StateResponse> histories;

    @JsonProperty(value = "uri")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String uri;

    public String getDeadlineExpiryDate() {
        return deadlineExpiryDate;
    }

    public void setDeadlineExpiryDate(Date deadlineExpiryDate) {
        this.deadlineExpiryDate = CohISO8601DateFormat.format(deadlineExpiryDate);
    }

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

    public List<AnswerResponse> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerResponse> answers) {
        this.answers = answers;
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
