package uk.gov.hmcts.reform.coh.controller.decision;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class CreateDecisionResponse {

    @JsonProperty(value = "decision_id")
    private UUID decisionId;

    public UUID getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(UUID decisionId) {
        this.decisionId = decisionId;
    }
}
