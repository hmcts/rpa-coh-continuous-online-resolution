package uk.gov.hmcts.reform.coh.controller.question;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuestionRequest {

    @JsonProperty("question_round")
    private String questionRound;

    @JsonProperty("question_ordinal")
    private String questionOrdinal;

    @JsonProperty("question_header_text")
    private String questionHeaderText;

    @JsonProperty("question_body_text")
    private String questionBodyText;

    @JsonProperty("owner_reference")
    private String ownerReference;

    public String getQuestionRound() {
        return questionRound;
    }

    public void setQuestionRound(String questionRound) {
        this.questionRound = questionRound;
    }

    public String getQuestionOrdinal() {
        return questionOrdinal;
    }

    public void setQuestionOrdinal(String questionOrdinal) {
        this.questionOrdinal = questionOrdinal;
    }

    public String getQuestionHeaderText() {
        return questionHeaderText;
    }

    public void setQuestionHeaderText(String questionHeaderText) {
        this.questionHeaderText = questionHeaderText;
    }

    public String getQuestionBodyText() {
        return questionBodyText;
    }

    public void setQuestionBodyText(String questionBodyText) {
        this.questionBodyText = questionBodyText;
    }

    public String getOwnerReference() {
        return ownerReference;
    }

    public void setOwnerReference(String ownerReference) {
        this.ownerReference = ownerReference;
    }
}
