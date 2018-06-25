package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class QuestionStateId implements Serializable {

    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "question_state_id")
    private Long questionStateId;


    public QuestionStateId(){}

    public QuestionStateId(Long questionId, Long questionStateId){
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


    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getQuestionStateId() {
        return questionStateId;
    }

    public void setQuestionStateId(Long questionStateId) {
        this.questionStateId = questionStateId;
    }
}
