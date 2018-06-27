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
    @MapsId("answerId")
    private Answer answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("answerStateId")
    private AnswerState answerstate;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_occured")
    private Date dateOccured = new Date();

    public AnswerStateHistory(Answer answer, AnswerState answerstate){
        this.answer = answer;
        this.answerstate = answerstate;
        this.id =  new AnswerStateId(answer.getAnswerId(), answerstate.getAnswerStateId());
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

    public AnswerState getAnswerstate() {
        return answerstate;
    }

    public void setAnswerstate(AnswerState answerstate) {
        this.answerstate = answerstate;
    }

    public Date getDateOccured() {
        return dateOccured;
    }

    public void setDateOccured(Date dateOccured) {
        this.dateOccured = dateOccured;
    }
}
