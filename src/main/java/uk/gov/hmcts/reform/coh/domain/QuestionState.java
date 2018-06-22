package uk.gov.hmcts.reform.coh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "question_state")
public class QuestionState {

    public static final int DRAFTED = 1;
    public static final int SUBMITTED = 2;
    public static final int ISSUED = 3;


    @Id
    @Column(name = "question_state_id")
    @JsonIgnore
    private int questionStateId;

    @Column(name = "state")
    @JsonProperty("state_name")
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

    public int getQuestionStateId() {
        return questionStateId;
    }

    public void setQuestionStateId(int questionStateId) {
        this.questionStateId = questionStateId;
    }
}
