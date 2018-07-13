package uk.gov.hmcts.reform.coh.functional.bdd.utils;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionRequest;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingRequest;
import uk.gov.hmcts.reform.coh.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ScenarioContext {

    private OnlineHearingRequest currentOnlineHearingRequest;

    private OnlineHearing currentOnlineHearing;

    private Question currentQuestion;

    private Answer currentAnswer;

    private DecisionRequest currentDecisionRequest;

    private Decision currentDecision;

    private List<String> caseIds;

    Set<Jurisdiction> jurisdictions;

    public OnlineHearingRequest getCurrentOnlineHearingRequest() {
        return currentOnlineHearingRequest;
    }

    public void setCurrentOnlineHearingRequest(OnlineHearingRequest currentOnlineHearingRequest) {
        this.currentOnlineHearingRequest = currentOnlineHearingRequest;
    }

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

    public DecisionRequest getCurrentDecisionRequest() {
        return currentDecisionRequest;
    }

    public void setCurrentDecisionRequest(DecisionRequest currentDecisionRequest) {
        this.currentDecisionRequest = currentDecisionRequest;
    }

    public void setCurrentAnswer(Answer currentAnswer) {
        this.currentAnswer = currentAnswer;
    }

    public Decision getCurrentDecision() {
        return currentDecision;
    }

    public void setCurrentDecision(Decision currentDecision) {
        this.currentDecision = currentDecision;
    }

    public List<String> getCaseIds() {
        return caseIds;
    }

    public void setCaseIds(List<String> caseId) {
        this.caseIds = caseId;
    }

    public void addCaseId(String caseId) {
        if (caseIds == null) {
            caseIds = new ArrayList<>();
        }
        caseIds.add(caseId);
    }

    public void setJurisdictions(Set<Jurisdiction> jurisdictions) {
        this.jurisdictions = jurisdictions;
    }

    public Set<Jurisdiction> getJurisdictions() {
        return jurisdictions;
    }

    public void clear() {
        currentOnlineHearing = null;
        currentQuestion = null;
        currentAnswer = null;
        caseIds = null;
        jurisdictions = null;
    }
}
