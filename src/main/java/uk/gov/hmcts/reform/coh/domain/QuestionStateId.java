package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class QuestionStateId implements Serializable {

    @Column(name = "question_id")
    private UUID questionId;

    @Column(name = "question_state_id")
    private int questionStateId;

    public QuestionStateId(){
    // Required for hibernate
    }

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
