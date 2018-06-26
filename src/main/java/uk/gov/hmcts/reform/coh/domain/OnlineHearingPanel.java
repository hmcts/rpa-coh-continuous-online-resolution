package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import java.util.UUID;

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

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;



    public Long getOnlineHearingPanelId() {
        return onlineHearingPanelId;
    }

    public void setOnlineHearingPanelId(Long onlineHearingPanelId) {
        this.onlineHearingPanelId = onlineHearingPanelId;
    }

    public OnlineHearing getOnlineHearing() {
        return onlineHearing;
    }

    public void setOnlineHearing(OnlineHearing onlineHearing) {
        this.onlineHearing = onlineHearing;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName() {
        this.fullName = firstName + " " + lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
