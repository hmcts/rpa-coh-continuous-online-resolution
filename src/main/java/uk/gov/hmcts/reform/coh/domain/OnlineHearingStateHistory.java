package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

    @Entity
    @Table(name = "online_hearing_state_history")
    public class OnlineHearingStateHistory {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        @Column(name = "id")
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "online_hearing_id")
        private OnlineHearing onlinehearing;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "online_hearing_state_id")
        private OnlineHearingState onlinehearingstate;

        @NotNull
        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "date_occurred")
        private Date dateOccurred = new Date();

        public OnlineHearingStateHistory() {
        }

        public OnlineHearing getOnlinehearing() {
            return onlinehearing;
        }

        public void setOnlinehearing(OnlineHearing onlinehearing) {
            this.onlinehearing = onlinehearing;
        }

        public OnlineHearingState getOnlinehearingstate() {
            return onlinehearingstate;
        }

        public void setOnlinehearingstate(OnlineHearingState onlinehearingstate) {
            this.onlinehearingstate = onlinehearingstate;
        }

        public OnlineHearingStateHistory(OnlineHearing onlinehearing,
                                         OnlineHearingState onlinehearingstate) {
            this.onlinehearing = onlinehearing;
            this.onlinehearingstate = onlinehearingstate;
        }

        public Date getDateOccurred() {
            return dateOccurred;
        }

        public void setDateOccurred(Date dateOccurred) {
            this.dateOccurred = dateOccurred;
        }
    }

