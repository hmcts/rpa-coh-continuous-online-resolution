package uk.gov.hmcts.reform.coh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "answer")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "answer_id")
    private UUID answerId;

    @Column(name = "answer_text")
    @JsonProperty("answer_text")
    private String answerText;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "question_id")
    @JsonIgnore
    private Question question;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "answer_state_id")
    @JsonProperty("current_answer_state")
    private AnswerState answerState;

    @OneToMany(mappedBy = "answer",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JsonIgnore
    private List<AnswerStateHistory> answerStateHistories = new ArrayList<>();

    public void registerStateChange(){
        AnswerStateHistory answerStateHistory = new AnswerStateHistory(this, answerState);
        answerStateHistories.add(answerStateHistory);
    }

    public UUID getAnswerId() {
        return answerId;
    }

    public void setAnswerId(UUID answerId) {
        this.answerId = answerId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public Answer answerId(UUID answerId) {
        this.answerId = answerId;
        return this;
    }

    public Answer answerText(String answerText) {
        this.answerText = answerText;
        return this;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public AnswerState getAnswerState() {
        return answerState;
    }

    public void setAnswerState(AnswerState answerState) {
        this.answerState = answerState;
    }

    public List<AnswerStateHistory> getAnswerStateHistories() {
        return answerStateHistories;
    }

    public void setAnswerStateHistories(List<AnswerStateHistory> answerStateHistories) {
        this.answerStateHistories = answerStateHistories;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "answerId=" + answerId +
                ", answerText='" + answerText + '\'' +
                ", question=" + question.toString() +
                ", answerState=" + answerState.toString() +
                ", answerStateHistories=" + answerStateHistories +
                '}';
    }

}
