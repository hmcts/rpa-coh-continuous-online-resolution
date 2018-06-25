package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import java.util.Objects;

@Entity(name = "Question State")
@Table(name = "question_state")
public class QuestionState {

    public static final Long DRAFTED = 1L;
    public static final Long ISSUED = 2L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "question_state_id")
    private Long questionStateId;

    @Column(name = "state")
    private String state;


    public QuestionState() {
    }

    public QuestionState(String state) {
        this.state = state;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

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

    public Long getQuestionStateId() {
        return questionStateId;
    }

    public void setQuestionStateId(Long questionStateId) {
        this.questionStateId = questionStateId;
    }
}
