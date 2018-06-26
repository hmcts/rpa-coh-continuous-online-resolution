package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import java.util.Date;
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

    public String getJurisdictionName() {
        return jurisdictionName;
    }

    public void setJurisdictionName(String jurisdictionName) {
        this.jurisdictionName = jurisdictionName;
    }

    @Column(name = "jurisdiction_id")
    private String jurisdictionId;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId("online_hearing_state_id")
    private OnlineHearingState onlineHearingState;

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

    @Override
    public String toString() {
        return "OnlineHearing{" +
                ", onlineHearingId=" + onlineHearingId +
                ", caseId='" + caseId + '\'' +
                '}';
    }


}
