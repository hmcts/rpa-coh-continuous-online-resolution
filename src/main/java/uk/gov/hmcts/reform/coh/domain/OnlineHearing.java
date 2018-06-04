package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "online_hearing")
public class OnlineHearing {

    @Id
    @Column(name = "online_hearing_id")
    private int onlineHearingId;

    @Column(name = "EXTERNAL_REF")
    private String externalRef;

    public int getOnlineHearingId() {
        return onlineHearingId;
    }

    public void setOnlineHearingId(int onlineHearingId) {
        this.onlineHearingId = onlineHearingId;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public void setExternalRef(String externalRef) {
        this.externalRef = externalRef;
    }
}
