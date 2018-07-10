package uk.gov.hmcts.reform.coh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "online_hearing")
public class Onlinehearing {

    @Id
    //@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "online_hearing_id")
    private UUID onlinehearingId;

    @Column(name = "case_id")
    @JsonProperty
    private String caseId;

    @ManyToOne(targetEntity = Jurisdiction.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "jurisdiction_id")
    private Jurisdiction jurisdiction;

    @OneToMany(mappedBy = "onlinehearing", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<OnlinehearingPanelMember> panelMembers;

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
    @JsonProperty("current_online_hearing_state")
    private Onlinehearingstate onlinehearingstate;

    @OneToMany(mappedBy = "onlinehearing",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JsonIgnore
    private List<OnlinehearingStateHistory> onlinehearingStateHistories = new ArrayList<>();

    public void registerStateChange(){
        OnlinehearingStateHistory onlinehearingStateHistory = new OnlinehearingStateHistory(this, onlinehearingstate);
        onlinehearingStateHistories.add(onlinehearingStateHistory);
    }

    public UUID getOnlinehearingId() {
        return onlinehearingId;
    }

    public void setOnlinehearingId(UUID onlinehearingId) {
        this.onlinehearingId = onlinehearingId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }


    public List<OnlinehearingPanelMember> getPanelMembers() {
        return panelMembers;
    }

    public void setPanelMembers(List<OnlinehearingPanelMember> panelMembers) {
        this.panelMembers = panelMembers;
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

    public Onlinehearingstate getOnlinehearingstate() {
        return onlinehearingstate;
    }

    public void setOnlinehearingstate(Onlinehearingstate onlinehearingstate) {
        this.onlinehearingstate = onlinehearingstate;
    }

    public String getOwnerReferenceId() {
        return ownerReferenceId;
    }

    public void setOwnerReferenceId(String ownerReferenceId) {
        this.ownerReferenceId = ownerReferenceId;
    }

    @Override
    public String toString() {
        return "Onlinehearing{" +
                "onlinehearingId=" + onlinehearingId +
                ", caseId='" + caseId + '\'' +
                ", jurisdiction=" + jurisdiction +
                ", jurisdictionName='" + jurisdictionName + '\'' +
                '}';
    }


    public void addState(Onlinehearingstate state) {
        this.onlinehearingstate = state;
        OnlinehearingStateHistory stateHistory = new OnlinehearingStateHistory(this, state);
        onlinehearingStateHistories.add(stateHistory);
    }

    public List<OnlinehearingStateHistory> getOnlinehearingStateHistories() {
        return onlinehearingStateHistories;
    }

    public void setJurisdictionName(String jurisdictionName) {
        this.jurisdictionName = jurisdictionName;
    }

    public String getJurisdictionName() {
        return this.jurisdictionName;
    }
}
