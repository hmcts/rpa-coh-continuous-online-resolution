package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "decision_reply")
public class DecisionReply {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "decision_reply_id")
    private UUID id;

    @Column(name = "author_reference_id")
    private String authorReferenceId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "decision_id")
    private Decision decision;

    @Column(name = "decision_reply")
    private String decisionReply;

    @Column(name = "decision_reply_reason")
    private String decisionReplyReason;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public String getDecisionReply() {
        return decisionReply;
    }

    public void setDecisionReply(String decisionReply) {
        this.decisionReply = decisionReply;
    }

    public String getDecisionReplyReason() {
        return decisionReplyReason;
    }

    public void setDecisionReplyReason(String decisionReplyReason) {
        this.decisionReplyReason = decisionReplyReason;
    }

    public String getAuthorReferenceId() {
        return authorReferenceId;
    }

    public void setAuthorReferenceId(String authorReferenceId) {
        this.authorReferenceId = authorReferenceId;
    }
}
