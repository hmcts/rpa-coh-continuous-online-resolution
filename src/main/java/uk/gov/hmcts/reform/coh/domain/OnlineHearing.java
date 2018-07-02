package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity(name = "Online Hearing")
@Table(name = "online_hearing")
public class OnlineHearing {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "online_hearing_id")
    private UUID onlineHearingId;

    @Column(name = "case_id")
    private String caseId;

    @ManyToOne(targetEntity = Jurisdiction.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "jurisdiction_id")
    private Jurisdiction jurisdiction;

    @Transient
    private String jurisdictionName;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId("online_hearing_state_id")
    private OnlineHearingState onlineHearingState;

    private List<OnlineHearingStateHistory> onlineHearingStateHistories = new ArrayList<>();

    @Column(name = "owner_reference_id")
    private String ownerReferenceId;

    public OnlineHearing() {
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

    public String getJurisdictionName() {
        return jurisdictionName;
    }

    public void setJurisdictionName(String jurisdictionName) {
        this.jurisdictionName = jurisdictionName;
    }

    @Override
    public String toString() {
        return "OnlineHearing{" +
                ", onlineHearingId=" + onlineHearingId +
                ", caseId='" + caseId + '\'' +
                '}';
    }


    public OnlineHearingState getOnlineHearingState() {
        return onlineHearingState;
    }

    public void setOnlineHearingState(OnlineHearingState onlineHearingState) {
        this.onlineHearingState = onlineHearingState;
    }

    public void addState(OnlineHearingState state) {
        this.onlineHearingState = state;
        OnlineHearingStateHistory stateHistory = new OnlineHearingStateHistory(this, state);
        onlineHearingStateHistories.add(stateHistory);
    }

    public List<OnlineHearingStateHistory> getOnlineHearingStateHistories() {
        return onlineHearingStateHistories;
    }

}
