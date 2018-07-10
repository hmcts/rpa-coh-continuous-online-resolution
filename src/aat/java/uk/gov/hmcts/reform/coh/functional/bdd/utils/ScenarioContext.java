package uk.gov.hmcts.reform.coh.functional.bdd.utils;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlinehearingRequest;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.Onlinehearing;
import uk.gov.hmcts.reform.coh.domain.Question;

import java.util.List;
import java.util.Set;

@Component
public class ScenarioContext {

    private Onlinehearing currentOnlinehearing;

    private Question currentQuestion;

    private Answer currentAnswer;

    private List<Onlinehearing> onlinehearings;

    Set<Jurisdiction> jurisdictions;

    public Onlinehearing getCurrentOnlinehearing() {
        return currentOnlinehearing;
    }

    public void setCurrentOnlinehearing(Onlinehearing currentOnlinehearing) {
        this.currentOnlinehearing = currentOnlinehearing;
    }

    public void setCurrentOnlinehearing(OnlinehearingRequest onlinehearingRequest) {
        currentOnlinehearing = new Onlinehearing();
        currentOnlinehearing.setCaseId(onlinehearingRequest.getCaseId());
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

    public List<Onlinehearing> getOnlinehearings() {
        return onlinehearings;
    }

    public void setOnlinehearings(List<Onlinehearing> onlinehearings) {
        this.onlinehearings = onlinehearings;
    }

    public void setJurisdictions(Set<Jurisdiction> jurisdictions) {
        this.jurisdictions = jurisdictions;
    }

    public Set<Jurisdiction> getJurisdictions() {
        return jurisdictions;
    }
}
