package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

    @Entity
    @Table(name = "online_hearing_state_history")
    public class OnlineHearingStateHistory {

        @EmbeddedId
        private OnlineHearingStateId id;

        @ManyToOne(fetch = FetchType.LAZY)
        @MapsId("onlineHearingId")
        private OnlineHearing onlinehearing;

        @ManyToOne(fetch = FetchType.LAZY)
        @MapsId("onlineHearingStateId")
        private OnlineHearingState onlinehearingstate;

        @NotNull
        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "date_occurred")
        private Date dateOccurred = new Date();

        public OnlineHearingStateHistory() {
        }

        public OnlineHearing getOnlineHearing() {
            return onlinehearing;
        }

        public void setOnlineHearing(OnlineHearing onlinehearing) {
            this.onlinehearing = onlinehearing;
        }

        public OnlineHearingState getOnlineHearingState() {
            return onlinehearingstate;
        }

        public void setOnlineHearingState(OnlineHearingState onlinehearingstate) {
            this.onlinehearingstate = onlinehearingstate;
        }

        public OnlineHearingStateHistory(OnlineHearing onlinehearing,
                                         OnlineHearingState onlinehearingstate) {
            this.onlinehearing = onlinehearing;
            this.onlinehearingstate = onlinehearingstate;
            this.id = new OnlineHearingStateId(onlinehearing.getOnlineHearingId(), onlinehearingstate.getOnlineHearingStateId());
        }

        public Date getDateOccurred() {
            return dateOccurred;
        }

        public void setDateOccurred(Date dateOccurred) {
            this.dateOccurred = dateOccurred;
        }

        public OnlineHearingStateId getId() {
            return id;
        }

        public void setId(OnlineHearingStateId id) {
            this.id = id;
        }
    }

