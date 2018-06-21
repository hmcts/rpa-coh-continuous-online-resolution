package uk.gov.hmcts.reform.coh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "question_round")
public class QuestionRound {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "question_round_id")
    private UUID questionRoundId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "online_hearing_id")
    @JsonIgnore
    private OnlineHearing onlineHearing;

    @Column(name = "round_number")
    private int roundNumber;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "question_round_state_id")
    @JsonIgnore
    private QuestionState questionState;

    public QuestionState getQuestionState() {
        return questionState;
    }

    public void setQuestionState(QuestionState questionState) {
        this.questionState = questionState;
    }

    public UUID getQuestionRoundId() {
        return questionRoundId;
    }

    public void setQuestionRoundId(UUID questionRoundId) {
        this.questionRoundId = questionRoundId;
    }

    public OnlineHearing getOnlineHearing() {
        return onlineHearing;
    }

    public void setOnlineHearing(OnlineHearing onlineHearing) {
        this.onlineHearing = onlineHearing;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }
}
