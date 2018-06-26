package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
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

    @OneToMany()
    @JoinColumn(name = "online_hearing_panel")
    private Set<OnlineHearingPanel> panel = new HashSet<>();


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



    @Override
    public String toString() {
        return "OnlineHearing{" +
                ", onlineHearingId=" + onlineHearingId +
                ", externalRef='" + externalRef + '\'' +
                '}';
    }

    public Set<OnlineHearingPanel> getPanel() {
        return panel;
    }

    public void setPanel(Set<OnlineHearingPanel> panel) {
        this.panel = panel;
    }
}
