package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;

@Entity
@Table(name = "online_hearing_panel")
public class OnlineHearingPanel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "online_hearing_panel_id")
    private Long onlineHearingPanelId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "online_hearing_id")
    private OnlineHearing onlineHearing;

    @Column(name = "name")
    private String name;

    @Column(name = "identity_reference")
    private String identityToken;


    public Long getOnlineHearingPanelId() {
        return onlineHearingPanelId;
    }

    public void setOnlineHearingPanelId(Long onlineHearingPanelId) {
        this.onlineHearingPanelId = onlineHearingPanelId;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentityToken() {
        return identityToken;
    }

    public void setIdentityToken(String identityToken) {
        this.identityToken = identityToken;
    }
}
