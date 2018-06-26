package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class AnswerStateId {
    @Column(name = "answer_id")
    private Long answerId;

    @Column(name = "answer_state_id")
    private int answerStateId;

    public AnswerStateId(Long answerId, int answerStateId){
        this.answerId = answerId;
        this.answerStateId = answerStateId;
    }

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public int getAnswerStateId() {
        return answerStateId;
    }

    public void setAnswerStateId(int answerStateId) {
        this.answerStateId = answerStateId;
    }
}
