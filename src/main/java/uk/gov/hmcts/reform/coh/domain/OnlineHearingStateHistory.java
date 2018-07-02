package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity(name = "Online Hearing State History")
@Table(name = "online_hearing_state_history")
public class OnlineHearingStateHistory {

    @EmbeddedId
    private OnlineHearingStateId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("online_hearing")
    private OnlineHearing onlineHearing;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("online_hearing_state_id")
    private OnlineHearingState onlineHearingState;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_occurred")
    private Date dateOccurred;

    private OnlineHearingStateHistory() {}

    public OnlineHearingStateHistory(OnlineHearing onlineHearing,
                                     OnlineHearingState onlineHearingState) {
        this.onlineHearing = onlineHearing;
        this.onlineHearingState = onlineHearingState;
    }

    public Date getDateOccurred() {
        return dateOccurred;
    }

    public void setDateOccurred(Date dateOccurred) {
        this.dateOccurred = dateOccurred;
    }
}
