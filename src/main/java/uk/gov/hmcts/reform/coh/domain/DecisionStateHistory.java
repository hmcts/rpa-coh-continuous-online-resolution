package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "decision_state_history")
public class DecisionStateHistory {

    @EmbeddedId
    private DecisionStateId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("decisionId")
    private Decision decision;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("decisionStateId")
    private DecisionState decisionState;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_occured")
    private Date dateOccured = new Date();

    public DecisionStateHistory(){
        // Required for hibernate
    }

    public DecisionStateHistory(Decision decision, DecisionState decisionState){
        this.decision = decision;
        this.decisionState = decisionState;
        this.id =  new DecisionStateId(decision.getDecisionId(), decisionState.getDecisionStateId());
    }

    public DecisionStateId getId() {
        return id;
    }

    public void setId(DecisionStateId id) {
        this.id = id;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public DecisionState getDecisionState() {
        return decisionState;
    }

    public void setDecisionState(DecisionState decisionState) {
        this.decisionState = decisionState;
    }

    public Date getDateOccured() {
        return dateOccured;
    }

    public void setDateOccured(Date dateOccured) {
        this.dateOccured = dateOccured;
    }
}