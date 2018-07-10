package uk.gov.hmcts.reform.coh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.*;

@Entity(name = "Question")
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "question_id")
    private UUID questionId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "online_hearing_id")
    @MapsId("onlineHearingId")
    @JsonIgnore
    private OnlineHearing onlineHearing;

    @Column(name = "question_ordinal")
    private int questionOrdinal;

    @Column(name = "question_header_text")
    private String questionHeaderText;

    @Column(name = "question_text")
    private String questionText;

    @Column(name = "question_round")
    private Integer questionRound;

    @Column(name = "deadline_expiry_date ")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deadlineExpiryDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_state_id")
    private QuestionState questionState;

    @Column(name = "author_reference_id")
    private String authorReferenceId;

    @Column(name = "owner_reference_id")
    private String ownerReferenceId ;

    @OneToMany(
            mappedBy = "question",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<QuestionStateHistory> questionStateHistories = new ArrayList<>();

    public Question() {}

    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    public int getQuestionOrdinal() {
        return questionOrdinal;
    }

    public void setQuestionOrdinal(int questionOrdinal) {
        this.questionOrdinal = questionOrdinal;
    }

    public String getQuestionHeaderText() {
        return questionHeaderText;
    }

    public void setQuestionHeaderText(String questionHeaderText) {
        this.questionHeaderText = questionHeaderText;
    }

    public Date getDeadlineExpiryDate() {
        return deadlineExpiryDate;
    }

    public void setDeadlineExpiryDate(Date deadlineExpiryDate) {
        this.deadlineExpiryDate = deadlineExpiryDate;
    }

    public String getAuthorReferenceId() {
        return authorReferenceId;
    }

    public void setAuthorReferenceId(String authorReferenceId) {
        this.authorReferenceId = authorReferenceId;
    }

    public String getOwnerReferenceId() {
        return ownerReferenceId;
    }

    public void setOwnerReferenceId(String ownerReferenceId) {
        this.ownerReferenceId = ownerReferenceId;
    }

    public void addState(QuestionState state) {
        this.questionState = state;
        QuestionStateHistory stateHistory = new QuestionStateHistory(this, state);
        questionStateHistories.add(stateHistory);
    }

    public List<QuestionStateHistory> getQuestionStateHistories() {
        return questionStateHistories;
    }

    public void setQuestionStateHistories(List<QuestionStateHistory> questionStateHistories) {
        this.questionStateHistories = questionStateHistories;
    }

    public Integer getQuestionRound() {
        return questionRound;
    }

    public void setQuestionRound(Integer questionRound) {
        this.questionRound = questionRound;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public QuestionState getQuestionState() {
        return questionState;
    }

    public void setQuestionState(QuestionState questionState) {
        this.questionState = questionState;
    }

    public OnlineHearing getOnlineHearing() {
        return onlineHearing;
    }

    public void setOnlineHearing(OnlineHearing onlineHearing) {
        this.onlineHearing = onlineHearing;
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Question question = (Question) o;
        return Objects.equals(questionId, question.questionId);
    }

    public Question questionId(UUID questionId) {
        this.questionId = questionId;
        return this;
    }

    public Question onlineHearing(OnlineHearing onlineHearing) {
        this.onlineHearing = onlineHearing;
        return this;
    }

    public Question questionOrdinal(int questionOrdinal) {
        this.questionOrdinal = questionOrdinal;
        return this;
    }

    public Question questionHeaderText(String questionHeaderText) {
        this.questionHeaderText = questionHeaderText;
        return this;
    }

    public Question questionText(String questionText) {
        this.questionText = questionText;
        return this;
    }

    public Question questionRound(int questionRound) {
        this.questionRound = questionRound;
        return this;
    }

    public Question deadlineExpiryDate(Date deadlineExpiryDate) {
        this.deadlineExpiryDate = deadlineExpiryDate;
        return this;
    }

    public Question questionState(QuestionState questionState) {
        this.questionState = questionState;
        return this;
    }

    public Question authorReferenceId(String authorReferenceId) {
        this.authorReferenceId = authorReferenceId;
        return this;
    }

    public Question ownerReferenceId(String ownerReferenceId) {
        this.ownerReferenceId = ownerReferenceId;
        return this;
    }

    public Question questionStateHistories(List<QuestionStateHistory> questionStateHistories) {
        this.questionStateHistories = questionStateHistories;
        return this;
    }
}