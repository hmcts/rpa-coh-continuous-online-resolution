package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "question_state_history")
public class QuestionStateHistory {

    @EmbeddedId
    private QuestionStateId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionStateId")
    private QuestionState questionstate; //lowercase as liquidbase is funny with case

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_occurred")
    private Date dateOccurred = new Date();


    private QuestionStateHistory() {}

    public QuestionStateHistory(Question question, QuestionState questionstate) {
        this.question = question;
        this.questionstate = questionstate;
        this.id = new QuestionStateId(question.getQuestionId(), questionstate.getQuestionStateId());
    }


    @Override
    public int hashCode() {
        return Objects.hash(question, questionstate);
    }


    public QuestionStateId getId() {
        return id;
    }

    public void setId(QuestionStateId id) {
        this.id = id;
    }

    public Date getDateOccurred() {
        return dateOccurred;
    }

    public void setDateOccurred(Date dateOccurred) {
        this.dateOccurred = dateOccurred;
    }
}
