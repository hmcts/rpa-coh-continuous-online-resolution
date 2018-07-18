package uk.gov.hmcts.reform.coh.controller.question;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateQuestionRequest {

    @JsonProperty("question_round")
    private int questionRound;

    @JsonProperty("question_ordinal")
    private int questionOrdinal;

    @JsonProperty("question_body_text")
    private String questionText;

    @JsonProperty("question_header_text")
    private String questionHeaderText;

    @JsonProperty("owner_reference")
    private String ownerReference;

    @JsonProperty("question_state")
    private String questionState;

    public int getQuestionRound() {
        return questionRound;
    }

    public void setQuestionRound(int questionRound) {
        this.questionRound = questionRound;
    }

    public int getQuestionOrdinal() {
        return questionOrdinal;
    }

    public void setQuestionOrdinal(int questionOrdinal) {
        this.questionOrdinal = questionOrdinal;
    }

    public String getOwnerReference() {
        return ownerReference;
    }

    public void setOwnerReference(String ownerReference) {
        this.ownerReference = ownerReference;
    }

    public String getQuestionState() {
        return questionState;
    }

    public void setQuestionState(String questionState) {
        this.questionState = questionState;
    }

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
