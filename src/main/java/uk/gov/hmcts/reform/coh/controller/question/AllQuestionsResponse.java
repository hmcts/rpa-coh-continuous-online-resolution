package uk.gov.hmcts.reform.coh.controller.question;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AllQuestionsResponse {

    @JsonProperty(value = "questions")
    private List<QuestionResponse> questions;

    public List<QuestionResponse> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionResponse> questions) {
        this.questions = questions;
    }
}
