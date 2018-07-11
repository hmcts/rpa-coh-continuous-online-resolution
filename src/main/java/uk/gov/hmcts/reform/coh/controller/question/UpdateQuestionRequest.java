package uk.gov.hmcts.reform.coh.controller.question;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateQuestionRequest {

    @JsonProperty("question_body_text")
    private String questionText;

    @JsonProperty("question_header_text")
    private String questionHeaderText;


    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public void setQuestionHeaderText(String questionHeaderText) {
        this.questionHeaderText = questionHeaderText;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getQuestionHeaderText() {
        return questionHeaderText;
    }

}
