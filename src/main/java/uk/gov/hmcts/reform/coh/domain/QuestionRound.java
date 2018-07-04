package uk.gov.hmcts.reform.coh.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class QuestionRound {

    @JsonProperty("question_round_number")
    private Integer questionRound;

    @JsonProperty("question_references")
    public List<Question> getQuestionList;

    public Integer getQuestionRound() {
        return questionRound;
    }

    public void setQuestionRoundNumber(Integer questionRound) {
        this.questionRound = questionRound;
    }

    public List<Question> getGetQuestionList() {
        return getQuestionList;
    }

    public void setGetQuestionList(List<Question> getQuestionList) {
        this.getQuestionList = getQuestionList;
    }
}
