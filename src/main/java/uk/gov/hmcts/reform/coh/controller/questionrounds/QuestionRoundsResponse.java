package uk.gov.hmcts.reform.coh.controller.questionrounds;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class QuestionRoundsResponse {

    @JsonProperty(value = "previous_question_round")
    private Integer previousQuestionRound;

    @JsonProperty(value = "current_question_round")
    private Integer currentQuestionRound;

    @JsonProperty(value = "next_question_round")
    private Integer nextQuestionRound;

    @JsonProperty(value = "max_number_of_question_rounds")
    private Integer maxQuestionRound;

    @JsonProperty(value = "question_rounds")
    private List<QuestionRoundResponse> questionRounds;

    public QuestionRoundsResponse(){
        questionRounds = new ArrayList<>();
    }

    public void setQuestionRounds(List<QuestionRoundResponse> questionRounds) {
        this.questionRounds = questionRounds;
    }

    public void addQuestionRoundResponse(QuestionRoundResponse questionRoundResponse){
        questionRounds.add(questionRoundResponse);
    }

    public Integer getPreviousQuestionRound() {
        return previousQuestionRound;
    }

    public void setPreviousQuestionRound(Integer previousQuestionRound) {
        this.previousQuestionRound = previousQuestionRound;
    }

    public Integer getCurrentQuestionRound() {
        return currentQuestionRound;
    }

    public void setCurrentQuestionRound(Integer currentQuestionRound) {
        this.currentQuestionRound = currentQuestionRound;
    }

    public Integer getNextQuestionRound() {
        return nextQuestionRound;
    }

    public void setNextQuestionRound(Integer nextQuestionRound) {
        this.nextQuestionRound = nextQuestionRound;
    }

    public Integer getMaxQuestionRound() {
        return maxQuestionRound;
    }

    public void setMaxQuestionRound(Integer maxQuestionRound) {
        this.maxQuestionRound = maxQuestionRound;
    }

    public List<QuestionRoundResponse> getQuestionRounds() {
        return questionRounds;
    }
}
