package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "question_state_history")
public class QuestionStateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_state_id")
    private QuestionState questionstate; //lowercase as liquidbase is funny with case

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_occurred")
    private Date dateOccurred = new Date();


    private QuestionStateHistory() {}

    public QuestionStateHistory(Question question, QuestionState questionstate) {
        this.question = question;
        this.questionstate = questionstate;
    }


    @Override
    public int hashCode() {
        return Objects.hash(question, questionstate);
    }

    public Date getDateOccurred() {
        return dateOccurred;
    }

    public void setDateOccurred(Date dateOccurred) {
        this.dateOccurred = dateOccurred;
    }
}
