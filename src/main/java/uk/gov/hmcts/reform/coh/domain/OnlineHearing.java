package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "online_hearing")
public class OnlineHearing {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "online_hearing_id")
    private UUID onlineHearingId;

    @Column(name = "EXTERNAL_REF")
    private String externalRef;

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

    public UUID getOnlineHearingId() {
        return onlineHearingId;
    }

    public void setOnlineHearingId(UUID onlineHearingId) {
        this.onlineHearingId = onlineHearingId;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public void setExternalRef(String externalRef) {
        this.externalRef = externalRef;
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
                "onlineHearingId=" + onlineHearingId +
                ", externalRef='" + externalRef + '\'' +
                ", jurisdiction=" + jurisdiction +
                ", jurisdictionName='" + jurisdictionName + '\'' +
                '}';
    }
}
