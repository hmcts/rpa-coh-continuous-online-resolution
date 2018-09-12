package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "online_hearing")
public class OnlineHearing {

    @Id
    @Column(name = "online_hearing_id")
    private UUID onlineHearingId;

    @Column(name = "case_id")
    private String caseId;

    @ManyToOne(targetEntity = Jurisdiction.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "jurisdiction_id")
    private Jurisdiction jurisdiction;

    @Transient
    private String jurisdictionName;

    @Column(name = "start_date", columnDefinition= "TIMESTAMP WITH TIME ZONE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Column(name = "end_date", columnDefinition= "TIMESTAMP WITH TIME ZONE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @Column(name = "owner_reference_id")
    private String ownerReferenceId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "online_hearing_state_id")
    private OnlineHearingState onlineHearingState;

    @Column(name = "relist_reason", columnDefinition="CLOB")
    private String relistReason;

    public void setOnlineHearingStateHistories(List<OnlineHearingStateHistory> onlineHearingStateHistories) {
        this.onlineHearingStateHistories = onlineHearingStateHistories;
    }

    @OneToMany(mappedBy = "onlinehearing",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<OnlineHearingStateHistory> onlineHearingStateHistories = new ArrayList<>();

    public void registerStateChange(){
        OnlineHearingStateHistory onlineHearingStateHistory = new OnlineHearingStateHistory(this, onlineHearingState);
        onlineHearingStateHistories.add(onlineHearingStateHistory);
    }

    public UUID getOnlineHearingId() {
        return onlineHearingId;
    }

    public void setOnlineHearingId(UUID onlineHearingId) {
        this.onlineHearingId = onlineHearingId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public Jurisdiction getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(Jurisdiction jurisdiction){
        this.jurisdiction = jurisdiction;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public OnlineHearingState getOnlineHearingState() {
        return onlineHearingState;
    }

    public void setOnlineHearingState(OnlineHearingState onlineHearingState) {
        this.onlineHearingState = onlineHearingState;
    }

    public String getOwnerReferenceId() {
        return ownerReferenceId;
    }

    public void setOwnerReferenceId(String ownerReferenceId) {
        this.ownerReferenceId = ownerReferenceId;
    }

    @Override
    public String toString() {
        return "OnlineHearing{" +
                "onlineHearingId=" + onlineHearingId +
                ", caseId='" + caseId + '\'' +
                ", jurisdiction=" + jurisdiction +
                ", jurisdictionName='" + jurisdictionName + '\'' +
                '}';
    }


    public void addOnlineHearingStateHistory(OnlineHearingState state) {
        OnlineHearingStateHistory stateHistory = new OnlineHearingStateHistory(this, state);
        onlineHearingStateHistories.add(stateHistory);
    }

    public List<OnlineHearingStateHistory> getOnlineHearingStateHistories() {
        return onlineHearingStateHistories;
    }

    public void setJurisdictionName(String jurisdictionName) {
        this.jurisdictionName = jurisdictionName;
    }

    public String getJurisdictionName() {
        return this.jurisdictionName;
    }

    public String getRelistReason() {
        return relistReason;
    }

    public void setRelistReason(String relistReason) {
        this.relistReason = relistReason;
    }
}
