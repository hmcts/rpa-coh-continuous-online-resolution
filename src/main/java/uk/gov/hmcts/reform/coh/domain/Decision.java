package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "decision")
public class Decision {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "decision_id")
    private UUID decisionId;

    @OneToOne(optional=false)
    @JoinColumn(name = "online_hearing_id")
    private OnlineHearing onlineHearing;

    @Column(name = "decision_header", length = 5000)
    private String decisionHeader;

    @Column(name = "decision_text", columnDefinition="CLOB NOT NULL")
    @Lob
    private String decisionText;

    @Column(name = "decision_reason", columnDefinition="CLOB NOT NULL")
    @Lob
    private String decisionReason;

    @Column(name = "decision_award", length = 5000)
    @Lob
    private String decisionAward;

    @ManyToOne(optional=false)
    @JoinColumn(name = "decision_state_id")
    private DecisionState decisionstate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "deadline_expiry_date", nullable = true)
    private Date deadlineExpiryDate;;

    @Column(name = "author_reference_id ")
    private String authorReferenceId ;

    @Column(name = "owner_reference_id  ")
    private String ownerReferenceId ;

    @OneToMany(mappedBy = "decision",
            cascade = CascadeType.ALL)
    private List<DecisionStateHistory> decisionStateHistories = new ArrayList<>();

    public UUID getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(UUID decisionId) {
        this.decisionId = decisionId;
    }

    public OnlineHearing getOnlineHearing() {
        return onlineHearing;
    }

    public void setOnlineHearing(OnlineHearing onlineHearing) {
        this.onlineHearing = onlineHearing;
    }

    public String getDecisionHeader() {
        return decisionHeader;
    }

    public void setDecisionHeader(String decisionHeader) {
        this.decisionHeader = decisionHeader;
    }

    public String getDecisionText() {
        return decisionText;
    }

    public void setDecisionText(String decisionText) {
        this.decisionText = decisionText;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }

    public String getDecisionAward() {
        return decisionAward;
    }

    public void setDecisionAward(String decisionAward) {
        this.decisionAward = decisionAward;
    }

    public DecisionState getDecisionstate() {
        return decisionstate;
    }

    public void setDecisionstate(DecisionState decisionstate) {
        this.decisionstate = decisionstate;
    }

    public Date getDeadlineExpiryDate() {
        return deadlineExpiryDate;
    }

    public void setDeadlineExpiryDate(Date deadlineExpiryDate) {
        this.deadlineExpiryDate = deadlineExpiryDate;
    }

    public String getAuthorReferenceId() {
        return authorReferenceId;
    }

    public void setAuthorReferenceId(String authorReferenceId) {
        this.authorReferenceId = authorReferenceId;
    }

    public String getOwnerReferenceId() {
        return ownerReferenceId;
    }

    public void setOwnerReferenceId(String ownerReferenceId) {
        this.ownerReferenceId = ownerReferenceId;
    }

    public List<DecisionStateHistory> getDecisionStateHistories() {
        return decisionStateHistories;
    }

    public void setDecisionStateHistories(List<DecisionStateHistory> decisionStateHistories) {
        this.decisionStateHistories = decisionStateHistories;
    }

    public boolean addDecisionStateHistory(DecisionState decisionState) {
       return  decisionStateHistories.add(new DecisionStateHistory(this, decisionState));
    }
}
