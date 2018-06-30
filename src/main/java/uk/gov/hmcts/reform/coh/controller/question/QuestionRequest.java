package uk.gov.hmcts.reform.coh.controller.question;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuestionRequest {

    @JsonProperty("question_round")
    private Integer questionRound;

    @JsonProperty("question_ordinal")
    private Integer questionOrdinal;

    @JsonProperty("question_header_text")
    private String questionHeaderText;

    @JsonProperty("question_body_text")
    private String questionBodyText;

    @JsonProperty("owner_reference")
    private String ownerReference;

    public Integer getQuestionRound() {
        return questionRound;
    }

    public void setQuestionRound(Integer questionRound) {
        this.questionRound = questionRound;
    }

    public Integer getQuestionOrdinal() {
        return questionOrdinal;
    }

    public void setQuestionOrdinal(Integer questionOrdinal) {
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
