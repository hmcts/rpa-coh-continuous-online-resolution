package uk.gov.hmcts.reform.coh.controller.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.coh.domain.QuestionState;

import java.util.HashMap;
import java.util.Map;

public class QuestionResponse extends QuestionRequest {

    @JsonProperty(value = "question_id")
    private String questionId;

//    @JsonProperty(value = "question_state")
//    private QuestionState questionState = new QuestionState();

    @JsonProperty(value = "current_question_state")
    private Map<String,String> currentState = new HashMap<>();

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

//    public QuestionState getQuestionState() {
//        return questionState;
//    }
//
//    public void setQuestionState(QuestionState questionState) {
//        this.questionState = questionState;
//    }


    public Map<String, String> getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String k , String v) {
        this.currentState.put(k,v);
    }
}
