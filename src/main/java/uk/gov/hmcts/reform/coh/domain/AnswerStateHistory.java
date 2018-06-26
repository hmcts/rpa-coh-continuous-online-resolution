package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "answer_state_history")
public class AnswerStateHistory {

    @EmbeddedId
    private AnswerStateId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "answer_id")
    private Answer answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "answer_state_id")
    private AnswerState answerState;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_occured")
    private Date dateOccured = new Date();

    public AnswerStateHistory(Answer answer, AnswerState answerState){
        this.answer = answer;
        this.answerState = answerState;
        this.id =  new AnswerStateId(answer.getAnswerId(), answerState.getAnswerStateId());
    }

    public AnswerStateId getId() {
        return id;
    }

    public void setId(AnswerStateId id) {
        this.id = id;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    public AnswerState getAnswerState() {
        return answerState;
    }

    public void setAnswerState(AnswerState answerState) {
        this.answerState = answerState;
    }

    public Date getDateOccured() {
        return dateOccured;
    }

    public void setDateOccured(Date dateOccured) {
        this.dateOccured = dateOccured;
    }
}
