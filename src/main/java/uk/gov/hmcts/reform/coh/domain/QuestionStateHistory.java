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
    @MapsId("question_id")
    private Question question;

    public QuestionStateId getId() {
        return id;
    }

    public void setId(QuestionStateId id) {
        this.id = id;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public QuestionState getQuestionState() {
        return questionState;
    }

    public void setQuestionState(QuestionState questionState) {
        this.questionState = questionState;
    }

    public Date getDateOccurred() {
        return dateOccurred;
    }

    public void setDateOccurred(Date dateOccurred) {
        this.dateOccurred = dateOccurred;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("question_state_id")
    private QuestionState questionState;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_occurred")
    private Date dateOccurred = new Date();


    private QuestionStateHistory() {}

    public QuestionStateHistory(Question question, QuestionState questionState) {
        this.question = question;
        this.questionState = questionState;
        this.id = new QuestionStateId(question.getQuestionId(), questionState.getQuestionStateId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        QuestionStateHistory that = (QuestionStateHistory) o;
        return Objects.equals(question, that.question) &&
                Objects.equals(questionState, that.questionState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(question, questionState);
    }


}
