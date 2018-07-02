package uk.gov.hmcts.reform.coh.controller.answer;

import java.util.UUID;

public class AnswerResponse {

    private UUID answerId;

    public UUID getAnswerId() {
        return answerId;
    }

    public void setAnswerId(UUID answerId) {
        this.answerId = answerId;
    }
}