package uk.gov.hmcts.reform.coh.controller.questionrounds;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.coh.controller.question.QuestionResponse;
import uk.gov.hmcts.reform.coh.domain.QuestionRoundState;

import java.util.ArrayList;
import java.util.List;

public class QuestionRoundResponse {

    @JsonProperty("question_round_number")
    private String questionRound;

    @JsonProperty("question_references")
    public List<QuestionResponse> questionList;

    @JsonProperty("question_round_state")
    private QuestionRoundState questionRoundState  = new QuestionRoundState();

    @JsonProperty(value = "deadline_extension_count")
    private Integer deadlineExtCount;

    public QuestionRoundResponse(){
        questionList = new ArrayList<>();
    }

    public void setQuestionRound(String questionRound) {
        this.questionRound = questionRound;
    }

    public String getQuestionRound() {
        return questionRound;
    }

    public void setQuestionRoundState(QuestionRoundState questionRoundState){
        this.questionRoundState = questionRoundState;
    }

    public QuestionRoundState getQuestionRoundState() {
        return questionRoundState;
    }

    public void setQuestionList(List<QuestionResponse> questionList) {
        this.questionList = questionList;
    }

    public List<QuestionResponse> getQuestionList(){
        return questionList;
    }

    public void addQuestionResponse(QuestionResponse questionResponse){
        questionList.add(questionResponse);
    }

    public Integer getDeadlineExtCount() {
        return deadlineExtCount;
    }

    public void setDeadlineExtCount(Integer deadlineExtCount) {
        this.deadlineExtCount = deadlineExtCount;
    }

}
