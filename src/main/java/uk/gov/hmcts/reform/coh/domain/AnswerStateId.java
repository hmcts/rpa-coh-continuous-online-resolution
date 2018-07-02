package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class AnswerStateId implements Serializable {
    @Column(name = "answer_id")
    private UUID answerId;

    @Column(name = "answer_state_id")
    private int answerStateId;

    public AnswerStateId(){
        //Required for hibernate
    }

    public AnswerStateId(UUID answerId, int answerStateId){
        this.answerId = answerId;
        this.answerStateId = answerStateId;
    }

    public UUID getAnswerId() {
        return answerId;
    }

    public void setAnswerId(UUID answerId) {
        this.answerId = answerId;
    }

    public int getAnswerStateId() {
        return answerStateId;
    }

    public void setAnswerStateId(int answerStateId) {
        this.answerStateId = answerStateId;
    }
}
