package uk.gov.hmcts.reform.coh.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "relisting_history")
public class RelistingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "online_hearing_id")
    private OnlineHearing onlineHearing;

    @Column(name = "relist_reason", columnDefinition = "CLOB")
    private String relistReason;

    @Enumerated
    @Column(name = "relist_state", columnDefinition = "smallint")
    private RelistingState relistState = RelistingState.DRAFTED;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_occurred")
    private Date dateOccurrred;

    public RelistingHistory() {
    }

    public RelistingHistory(OnlineHearing onlineHearing, String relistReason, RelistingState relistState, Date now) {
        this.onlineHearing = onlineHearing;
        this.relistReason = relistReason;
        this.relistState = relistState;
        this.dateOccurrred = now;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OnlineHearing getOnlineHearing() {
        return onlineHearing;
    }

    public void setOnlineHearing(OnlineHearing onlineHearing) {
        this.onlineHearing = onlineHearing;
    }

    public String getRelistReason() {
        return relistReason;
    }

    public void setRelistReason(String relistReason) {
        this.relistReason = relistReason;
    }

    public RelistingState getRelistState() {
        return relistState;
    }

    public void setRelistState(RelistingState relistState) {
        this.relistState = relistState;
    }

    public Date getDateOccurrred() {
        return dateOccurrred;
    }

    public void setDateOccurrred(Date dateOccurrred) {
        this.dateOccurrred = dateOccurrred;
    }
}
