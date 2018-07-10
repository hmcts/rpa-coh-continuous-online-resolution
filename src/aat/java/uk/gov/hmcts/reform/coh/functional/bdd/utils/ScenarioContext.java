package uk.gov.hmcts.reform.coh.functional.bdd.utils;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.controller.onlineHearing.OnlineHearingRequest;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;

import java.util.List;
import java.util.Set;

@Component
public class ScenarioContext {

    private OnlineHearing currentOnlineHearing;

    private Question currentQuestion;

    private Answer currentAnswer;

    private List<OnlineHearing> onlineHearings;

    Set<Jurisdiction> jurisdictions;

    public OnlineHearing getCurrentOnlineHearing() {
        return currentOnlineHearing;
    }

    public void setCurrentOnlineHearing(OnlineHearing currentOnlineHearing) {
        this.currentOnlineHearing = currentOnlineHearing;
    }

    public void setCurrentOnlineHearing(OnlineHearingRequest onlineHearingRequest) {
        currentOnlineHearing = new OnlineHearing();
        currentOnlineHearing.setCaseId(onlineHearingRequest.getCaseId());
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

    public void setJurisdictions(Set<Jurisdiction> jurisdictions) {
        this.jurisdictions = jurisdictions;
    }

    public Set<Jurisdiction> getJurisdictions() {
        return jurisdictions;
    }
}
