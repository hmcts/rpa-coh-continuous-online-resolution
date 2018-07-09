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
    private DecisionState decisionstate; // JPA cares about the case

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_occured")
    private Date dateOccured = new Date();

    public DecisionStateHistory(){
        // Required for hibernate
    }

    public DecisionStateHistory(Decision decision, DecisionState decisionState){
        this.decision = decision;
        this.decisionstate = decisionState;
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

    public DecisionState getDecisionstate() {
        return decisionstate;
    }

    public void setDecisionstate(DecisionState decisionstate) {
        this.decisionstate = decisionstate;
    }

    public Date getDateOccured() {
        return dateOccured;
    }

    public void setDateOccured(Date dateOccured) {
        this.dateOccured = dateOccured;
    }
}