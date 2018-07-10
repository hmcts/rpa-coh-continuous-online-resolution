package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;

@Entity
@Table(name = "online_hearing_panel_member")
public class OnlinehearingPanelMember {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "online_hearing_panel_id", nullable = false)
    private Long onlinehearingPanelId;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "online_hearing_id")
    private Onlinehearing onlinehearing;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "identity_reference")
    private String identityToken;

    public Long getOnlinehearingPanelId() {
        return onlinehearingPanelId;
    }

    public void setOnlinehearingPanelId(Long onlinehearingPanelId) {
        this.onlinehearingPanelId = onlinehearingPanelId;
    }

    public Onlinehearing getOnlinehearing() {
        return onlinehearing;
    }

    public void setOnlinehearing(Onlinehearing onlinehearing) {
        this.onlinehearing = onlinehearing;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getIdentityToken() {
        return identityToken;
    }

    public void setIdentityToken(String identityToken) {
        this.identityToken = identityToken;
    }
}