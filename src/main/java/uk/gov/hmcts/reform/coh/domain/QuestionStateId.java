package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class QuestionStateId implements Serializable {

    @Column(name = "question_id")
    private UUID questionId;

    @Column(name = "question_state_id")
    private int questionStateId;


    public QuestionStateId(){}

    public QuestionStateId(UUID questionId, int questionStateId){
        this.questionId = questionId;
        this.questionStateId = questionStateId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        QuestionStateId that = (QuestionStateId) o;
        return Objects.equals(questionId, that.questionId) &&
                Objects.equals(questionStateId, that.questionStateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId, questionStateId);
    }


    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    public int getQuestionStateId() {
        return questionStateId;
    }

    public void setQuestionStateId(int questionStateId) {
        this.questionStateId = questionStateId;
    }
}
