package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;

@Entity
@Table(name = "question_state")
public class QuestionState {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "question_state_id")
    private int questionId;

    @Column(name = "state")
    private String state;

}
