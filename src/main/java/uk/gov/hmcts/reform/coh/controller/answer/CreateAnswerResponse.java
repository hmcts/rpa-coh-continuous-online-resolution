package uk.gov.hmcts.reform.coh.controller.answer;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class CreateAnswerResponse {

    @JsonProperty(value = "answer_id")
    private UUID answerId;

    public UUID getAnswerId() {
        return answerId;
    }

    public void setAnswerId(UUID answerId) {
        this.answerId = answerId;
    }
}