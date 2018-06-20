package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "question_round")
public class QuestionRound {

    @Id
    @Column(name = "question_round_id")
    private int questionRoundId;

    @Column(name = "online_hearing_id")
    private int onlineHearingId;

    @Column(name = "round_number")
    private int roundNumber;

    public int getQuestionRoundId() {
        return questionRoundId;
    }

    public void setQuestionRoundId(int questionRoundId) {
        this.questionRoundId = questionRoundId;
    }

    public int getOnlineHearingId() {
        return onlineHearingId;
    }

    public void setOnlineHearingId(int onlineHearingId) {
        this.onlineHearingId = onlineHearingId;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }
}
