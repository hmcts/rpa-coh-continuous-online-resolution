package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "online_hearing")
public class OnlineHearing {

    @Id
    @SequenceGenerator(name="online_hearing_id_seq",
            sequenceName="online_hearing_id_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator="online_hearing_id_seq")
    @Column(name = "online_hearing_id", updatable=false)
    private Long onlineHearingId;

    @Column(name = "EXTERNAL_REF")
    private String externalRef;


    public Long getOnlineHearingId() {
        return onlineHearingId;
    }

    public void setOnlineHearingId(Long onlineHearingId) {
        this.onlineHearingId = onlineHearingId;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public void setExternalRef(String externalRef) {
        this.externalRef = externalRef;
    }

    @Override
    public String toString() {
        return "OnlineHearing{" +
                ", onlineHearingId=" + onlineHearingId +
                ", externalRef='" + externalRef + '\'' +
                '}';
    }


}
