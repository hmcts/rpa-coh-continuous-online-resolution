package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

    @Entity
    @Table(name = "online_hearing_state_history")
    public class OnlinehearingStateHistory {

        @EmbeddedId
        private OnlinehearingStateId id;

        @ManyToOne(fetch = FetchType.LAZY)
        @MapsId("onlinehearingId")
        private Onlinehearing onlinehearing;

        @ManyToOne(fetch = FetchType.LAZY)
        @MapsId("onlinehearingStateId")
        private Onlinehearingstate onlinehearingstate;

        @NotNull
        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "date_occurred")
        private Date dateOccurred = new Date();

        public OnlinehearingStateHistory() {
        }

//        public OnlinehearingStateId getId() {
//            return id;
//        }

//        public void setId(OnlinehearingStateId id) {
//            this.id = id;
//        }

        public Onlinehearing getOnlinehearing() {
            return onlinehearing;
        }

        public void setOnlinehearing(Onlinehearing onlinehearing) {
            this.onlinehearing = onlinehearing;
        }

        public Onlinehearingstate getOnlinehearingstate() {
            return onlinehearingstate;
        }

        public void setOnlinehearingstate(Onlinehearingstate onlinehearingstate) {
            this.onlinehearingstate = onlinehearingstate;
        }

        public OnlinehearingStateHistory(Onlinehearing onlinehearing,
                                         Onlinehearingstate onlinehearingstate) {
            this.onlinehearing = onlinehearing;
            this.onlinehearingstate = onlinehearingstate;
            this.id = new OnlinehearingStateId(onlinehearing.getOnlinehearingId(), onlinehearingstate.getOnlinehearingStateId());
        }

        public Date getDateOccurred() {
            return dateOccurred;
        }

        public void setDateOccurred(Date dateOccurred) {
            this.dateOccurred = dateOccurred;
        }

        public OnlinehearingStateId getId() {
            return id;
        }

        public void setId(OnlinehearingStateId id) {
            this.id = id;
        }
    }

