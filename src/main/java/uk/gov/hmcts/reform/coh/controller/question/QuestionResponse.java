package uk.gov.hmcts.reform.coh.controller.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import uk.gov.hmcts.reform.coh.controller.state.StateResponse;
import uk.gov.hmcts.reform.coh.domain.Answer;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class QuestionResponse extends QuestionRequest {

    @JsonProperty(value = "question_id")
    private String questionId;

    @JsonProperty(value = "deadline_expiry_date")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String deadlineExpiryDate;

    @JsonProperty(value = "current_question_state")
    private StateResponse currentState = new StateResponse();

    @JsonProperty(value = "answer")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Answer> answers;

    public String getDeadlineExpiryDate() {
        return deadlineExpiryDate;
    }

    public void setDeadlineExpiryDate(Date deadlineExpiryDate) {
        ISO8601DateFormat formatter = new ISO8601DateFormat();
        String res = formatter.format(deadlineExpiryDate);
        this.deadlineExpiryDate = res;
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

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }
}
