package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(name = "Question")
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "question_id")
    private int questionId;

    @Column(name = "question_round_id")
    private int questionRoundId;

    @Column(name = "online_hearing_id")
    private int onlineHearingId;

    @Column(name = "subject")
    private String subject;

    @Column(name = "question_text")
    private String questionText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_state_id")
    private QuestionState questionState;

    @OneToMany(
            mappedBy = "question",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<QuestionStateHistory> questionStateHistories = new ArrayList<>();


    public Question() {}

    public Question(String subject) {
        this.subject = subject;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Question question = (Question) o;
        return Objects.equals(subject, question.subject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject);
    }


    public void addState(QuestionState state) {
        this.questionState = state;
        QuestionStateHistory stateHistory = new QuestionStateHistory(this, state);
        questionStateHistories.add(stateHistory);
    }

//    public void removeState(QuestionState state) {
//        for (Iterator<QuestionStateHistory> iterator = questionStateHistories.iterator();
//             iterator.hasNext(); ) {
//            QuestionStateHistory stateHistory = iterator.next();
//
//            if (stateHistory.getQuestion().equals(this) &&
//                    stateHistory.getQuestionstate().equals(state)) {
//                iterator.remove();
//                stateHistory.setQuestion(null);
//                stateHistory.setQuestionstate(null);
//            }
//        }
//    }

    public List<QuestionStateHistory> getQuestionStateHistories() {
        return questionStateHistories;
    }

    public void setQuestionStateHistories(List<QuestionStateHistory> questionStateHistories) {
        this.questionStateHistories = questionStateHistories;
    }

    public int getQuestionRoundId() {
        return questionRoundId;
    }

    public void setQuestionRoundId(int questionRoundId) {
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

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getOnlineHearingId() {
        return onlineHearingId;
    }

    public void setOnlineHearingId(int onlineHearingId) {
        this.onlineHearingId = onlineHearingId;
    }

    public QuestionState getQuestionState() {
        return questionState;
    }

    public void setQuestionState(QuestionState questionState) {
        this.questionState = questionState;
    }
}
