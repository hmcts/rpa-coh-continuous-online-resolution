package uk.gov.hmcts.reform.coh.functional.bdd.utils;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;

import java.util.List;

@Component
public class ScenarioContext {

    private OnlineHearing currentOnlineHearing;

    private Question currentQuestion;

    private Answer currentAnswer;

    private List<OnlineHearing> onlineHearings;

    public OnlineHearing getCurrentOnlineHearing() {
        return currentOnlineHearing;
    }

    public void setCurrentOnlineHearing(OnlineHearing currentOnlineHearing) {
        this.currentOnlineHearing = currentOnlineHearing;
    }

    public Question getCurrentQuestion() {
        return currentQuestion;
    }

    public void setCurrentQuestion(Question currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    public Answer getCurrentAnswer() {
        return currentAnswer;
    }

    public void setCurrentAnswer(Answer currentAnswer) {
        this.currentAnswer = currentAnswer;
    }

    public List<OnlineHearing> getOnlineHearings() {
        return onlineHearings;
    }

    public void setOnlineHearings(List<OnlineHearing> onlineHearings) {
        this.onlineHearings = onlineHearings;
    }
}
