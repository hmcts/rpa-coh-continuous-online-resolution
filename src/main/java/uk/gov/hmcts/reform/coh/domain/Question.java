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
    private Long questionId;

    @Column(name = "question_round_id")
    private int questionRoundId;

    @ManyToOne(targetEntity = OnlineHearing.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "online_hearing")
    private OnlineHearing onlineHearing;

    @Column(name = "subject")
    private String subject;

    @Column(name = "question_text")
    private String questionText;

    @ManyToOne(fetch = FetchType.EAGER)
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

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public OnlineHearing getOnlineHearing() {
        return onlineHearing;
    }

    public void setOnlineHearing(OnlineHearing onlineHearingId) {
        this.onlineHearing = onlineHearingId;
    }

    public QuestionState getQuestionState() {
        return questionState;
    }

    public void setQuestionState(QuestionState questionState) {
        this.questionState = questionState;
    }

    public Question questionId(Long questionId) {
        this.questionId = questionId;
        return this;
    }
}