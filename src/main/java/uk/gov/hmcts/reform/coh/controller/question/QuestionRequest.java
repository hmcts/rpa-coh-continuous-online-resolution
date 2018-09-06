package uk.gov.hmcts.reform.coh.controller.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Set;
import java.util.UUID;

public class QuestionRequest {

    @JsonProperty("question_round")
    @ApiModelProperty(required = true, allowableValues = "0, 1, 2, ...", notes = "Positive integer")
    private String questionRound;

    @JsonProperty("question_ordinal")
    @ApiModelProperty(required = true, allowableValues = "0, 1, 2, ...", notes = "Positive integer")
    private String questionOrdinal;

    @JsonProperty("question_header_text")
    @ApiModelProperty(required = true)
    private String questionHeaderText;

    @JsonProperty("question_body_text")
    @ApiModelProperty(required = true)
    private String questionBodyText;

    @JsonProperty("linked_question_id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<UUID> linkedQuestionId;

    @JsonProperty("owner_reference")
    @ApiModelProperty(required = true)
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

    public Set<UUID> getLinkedQuestionId() {
        return linkedQuestionId;
    }

    public void setLinkedQuestionId(Set<UUID> linkedQuestionId) {
        this.linkedQuestionId = linkedQuestionId;
    }

    public String getOwnerReference() {
        return ownerReference;
    }

    public void setOwnerReference(String ownerReference) {
        this.ownerReference = ownerReference;
    }
}
