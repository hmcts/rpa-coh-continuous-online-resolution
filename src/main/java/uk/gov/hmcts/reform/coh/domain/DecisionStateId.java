package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class DecisionStateId implements Serializable {
    @Column(name = "decision_id")
    private UUID decisionId;

    @Column(name = "decision_state_id")
    private int decisionStateId;

    public DecisionStateId(){}

    public DecisionStateId(UUID decisionId, int decisionStateId){
        this.decisionId = decisionId;
        this.decisionStateId = decisionStateId;
    }
}
