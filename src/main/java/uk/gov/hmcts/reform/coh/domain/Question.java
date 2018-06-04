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
    private int questionId;

    @Column(name = "question_round_id")
    private int questionRoundId;

    @Column(name = "question_text")
    private String questionText;
}
