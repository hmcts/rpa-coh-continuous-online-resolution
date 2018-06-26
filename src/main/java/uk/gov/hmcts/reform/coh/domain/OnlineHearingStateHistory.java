package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity(name = "Online Hearing State History")
@Table(name = "online_hearing_state_history")
public class OnlineHearingStateHistory {

    @EmbeddedId
    private OnlineHearingStateType id;

    @ManyToOne
    @MapsId
    private OnlineHearing onlineHearing;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("onlineHearingStateId")
    private OnlineHearingStateType onlineHearingStateType;

    @Column(name = "date_occurred")
    private Date dateOccurred;

    private OnlineHearingStateHistory() {}

    public OnlineHearingStateHistory(OnlineHearing onlineHearing,
                                     OnlineHearingStateType onlineHearingStateType,
                                     Date dateOccurred) {
        this.onlineHearing = onlineHearing;
        this.onlineHearingStateType = onlineHearingStateType;
        this.dateOccurred = dateOccurred;
    }

    public OnlineHearingStateType getState() {
        return id;
    }

    public void setState(OnlineHearingStateType id) {
        this.id = id;
    }

    public Date getDateOccurred() {
        return dateOccurred;
    }

    public void setDateOccurred(Date dateOccurred) {
        this.dateOccurred = dateOccurred;
    }
}
