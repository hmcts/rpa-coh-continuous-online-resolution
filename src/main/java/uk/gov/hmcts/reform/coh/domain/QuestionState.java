package uk.gov.hmcts.reform.coh.domain;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Cache;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(name = "Question State")
@Table(name = "question_state")
//@NaturalIdCache
//@Cache( usage = CacheConcurrencyStrategy.READ_WRITE)
public class QuestionState {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "question_state_id")
    private int questionStateId;

    @Column(name = "state")
    private String state;

    @OneToMany(
            mappedBy = "questionState",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<QuestionStateHistory> questions = new ArrayList<>();

    public QuestionState() {
    }

    public QuestionState(String state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionState questionState = (QuestionState) o;
        return Objects.equals(state, questionState.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state);
    }



    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getQuestionStateId() {
        return questionStateId;
    }

    public void setQuestionStateId(int questionStateId) {
        this.questionStateId = questionStateId;
    }

    public List<QuestionStateHistory> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionStateHistory> questions) {
        this.questions = questions;
    }
}
