package uk.gov.hmcts.reform.coh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "question_round")
public class QuestionRound {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "question_round_id")
    @JsonProperty("question_round_id")
    private UUID questionRoundId;

    @Column(name = "round_number")
    @JsonProperty("question_round_number")
    private int roundNumber;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "online_hearing_id")
    @JsonIgnore
    private OnlineHearing onlineHearing;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "question_round_state_id")
    @JsonProperty("question_state")
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

    @Override
    public String toString() {
        return "QuestionRound{" +
                "questionRoundId=" + questionRoundId +
                ", roundNumber=" + roundNumber +
                ", onlineHearing=" + onlineHearing +
                ", questionState=" + questionState +
                '}';
    }
}
