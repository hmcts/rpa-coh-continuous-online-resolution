package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "question")
public class Question {

    @Id
    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "question_round_id", nullable = true)
    private Long questionRoundId;

    @Column(name = "subject")
    private String subject;

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getQuestionRoundId() {
        return questionRoundId;
    }

    public void setQuestionRoundId(Long questionRoundId) {
        this.questionRoundId = questionRoundId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    @Column(name = "question_text")
    private String questionText;
}
