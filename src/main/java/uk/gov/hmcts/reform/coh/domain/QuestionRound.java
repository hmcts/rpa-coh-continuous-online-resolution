package uk.gov.hmcts.reform.coh.domain;

import java.util.List;

public class QuestionRound {

    private Integer questionRound;

    private List<Question> questionList;
    private QuestionRoundState questionRoundState;

    public void setQuestionRound(Integer questionRound) {
        this.questionRound = questionRound;
    }

    public QuestionRoundState getQuestionRoundState() {
        return questionRoundState;
    }

    public void setQuestionRoundState(QuestionRoundState questionRoundState) {
        this.questionRoundState = questionRoundState;
    }

    public Integer getQuestionRound() {
        return questionRound;
    }

    public void setQuestionRoundNumber(Integer questionRound) {
        this.questionRound = questionRound;
    }

    public List<Question> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<Question> questionList) {
        this.questionList = questionList;
    }
}
