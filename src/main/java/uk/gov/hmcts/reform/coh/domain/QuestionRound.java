package uk.gov.hmcts.reform.coh.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class QuestionRound {

    @JsonProperty(value = "current_question_round")
    private Integer currentQuestionRound;

    @JsonProperty(value = "next_question_round")
    private Integer nextQuestionRound;

    @JsonProperty(value = "question_references")
    private List<Question> questionList;

    public List<Question> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<Question> questionList) {
        this.questionList = questionList;
        setCurrentQuestionRound(questionList.get(0).getQuestionRound());
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
}
