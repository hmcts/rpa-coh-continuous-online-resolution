package uk.gov.hmcts.reform.coh.controller.question;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AllQuestionsResponse {

    @JsonProperty(value = "questions")
    private List<QuestionAndAnswerResponse> questions;

    public List<QuestionAndAnswerResponse> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionAndAnswerResponse> questions) {
        this.questions = questions;
    }
}
