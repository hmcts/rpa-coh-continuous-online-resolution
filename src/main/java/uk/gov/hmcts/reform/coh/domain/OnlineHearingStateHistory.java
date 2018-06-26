package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "Online Hearing State History")
@Table(name = "online_hearing_state_history")
public class OnlineHearingStateHistory {

    @EmbeddedId
    private OnlineHearingState id;

    @ManyToOne
    @MapsId
    private OnlineHearing onlineHearing;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("onlineHearingStateId")
    private OnlineHearingState onlineHearingState;

    @Column(name = "date_occurred")
    private Date dateOccurred;

    private OnlineHearingStateHistory() {}

    public OnlineHearingStateHistory(OnlineHearing onlineHearing,
                                     OnlineHearingState onlineHearingState,
                                     Date dateOccurred) {
        this.onlineHearing = onlineHearing;
        this.onlineHearingState = onlineHearingState;
        this.dateOccurred = dateOccurred;
    }

    public OnlineHearingState getState() {
        return id;
    }

    public void setState(OnlineHearingState id) {
        this.id = id;
    }

    public Date getDateOccurred() {
        return dateOccurred;
    }

    public void setDateOccurred(Date dateOccurred) {
        this.dateOccurred = dateOccurred;
    }
}
