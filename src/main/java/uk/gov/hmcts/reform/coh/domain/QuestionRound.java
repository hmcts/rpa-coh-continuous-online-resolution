package uk.gov.hmcts.reform.coh.domain;

import java.util.ArrayList;
import java.util.List;

public class QuestionRound {

    private OnlineHearing onlineHearing;

    private Integer questionRoundNumber;

    private List<Question> questionList = new ArrayList<>();

    private QuestionRoundState questionRoundState;

    public OnlineHearing getOnlineHearing() {
        return onlineHearing;
    }

    public void setOnlineHearing(OnlineHearing onlineHearing) {
        this.onlineHearing = onlineHearing;
    }

    public Integer getQuestionRoundNumber() {
        return questionRoundNumber;
    }

    public void setQuestionRoundNumber(Integer questionRoundNumber) {
        this.questionRoundNumber = questionRoundNumber;
    }

    public List<Question> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<Question> questionList) {
        this.questionList = questionList;
    }

    public QuestionRoundState getQuestionRoundState() {
        return questionRoundState;
    }

    public void setQuestionRoundState(QuestionRoundState questionRoundState) {
        this.questionRoundState = questionRoundState;
    }
}
