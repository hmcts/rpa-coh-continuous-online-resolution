package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "answer")
public class Answer {

    @Id
    @Column(name = "answer_id")
    private int answerId;

    @Column(name = "question_id")
    private int questionId;

    @Column(name = "answer_text")
    private String answerText;
}
