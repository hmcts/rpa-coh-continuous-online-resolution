package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.controller.exceptions.NotAValidUpdateException;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;
import uk.gov.hmcts.reform.coh.service.utils.ExpiryCalendar;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import javax.persistence.EntityNotFoundException;
import java.util.*;

@Service
public class QuestionRoundService {

    private QuestionRepository questionRepository;

    private QuestionStateService questionStateService;

    private AnswerService answerService;

    public static final String DRAFTED = QuestionStates.DRAFTED.getStateName();
    public static final String ISSUE_PENDING = QuestionStates.ISSUE_PENDING.getStateName();
    public static final String ISSUED = QuestionStates.ISSUED.getStateName();
    public static final String QUESTIONS_ANSWERED = "questions_answered";


    public QuestionRoundService() {}

    @Autowired
    public QuestionRoundService(QuestionRepository questionRepository,
                                QuestionStateService questionStateService,
                                AnswerService answerService) {
        this.questionRepository = questionRepository;
        this.questionStateService = questionStateService;
        this.answerService = answerService;
    }

    public boolean alreadyIssued(QuestionRoundState questionRoundState) {

        return questionRoundState.getState().equals(ISSUED)
                || questionRoundState.getState().equals(QUESTIONS_ANSWERED)
                || questionRoundState.getState().equals(QuestionStates.DEADLINE_ELAPSED.getStateName())
                || questionRoundState.getState().equals(QuestionStates.QUESTION_DEADLINE_EXTENSION_GRANTED.getStateName())
                || questionRoundState.getState().equals(QuestionStates.QUESTION_DEADLINE_EXTENSION_DENIED.getStateName());
    }

    public boolean alreadyIssuedOrPending(QuestionRoundState questionRoundState) {

        return alreadyIssued(questionRoundState) || questionRoundState.getState().equals(ISSUE_PENDING);
    }

    public boolean isFirstRound(int currentRoundNumber) {
        return currentRoundNumber == 0;
    }

    @Transactional
    public boolean isQrValidState(Question question, OnlineHearing onlineHearing) {
        int targetQuestionRound = question.getQuestionRound();
        int currentRoundNumber = getCurrentQuestionRoundNumber(onlineHearing);

        QuestionRoundState currentState = retrieveQuestionRoundState(getQuestionRoundByRoundId(onlineHearing, currentRoundNumber));

        if(!isFirstRound(currentRoundNumber) && isIncremented(question.getQuestionRound(), currentRoundNumber)
                && !alreadyIssued(currentState)){
            throw new NotAValidUpdateException("Cannot increment question round unless previous question round is issued");
        }
        else if ((alreadyIssuedOrPending(currentState) && isIncremented(targetQuestionRound, currentRoundNumber))
            || (!alreadyIssuedOrPending(currentState) && targetQuestionRound == currentRoundNumber || isFirstRound(currentRoundNumber)) ) {
            // Current QR is issued and create new question round
            // or
            // Current QR is not issued and question is current question round OR no QR exists yet
            return true;
        }

        return false;
    }

    @Transactional
    public boolean isQrValidTransition(Question question, OnlineHearing onlineHearing) {
        if (question.getQuestionRound() == null || question.getQuestionRound() == 0) {
            throw new EntityNotFoundException();
        }
        Jurisdiction jurisdiction = onlineHearing.getJurisdiction();

        int maxQuestionRounds = jurisdiction.getMaxQuestionRounds();
        int targetQuestionRound = question.getQuestionRound();
        int currentQuestionRound = getCurrentQuestionRoundNumber(onlineHearing);

        if (isFirstRound(currentQuestionRound)) {
            return (targetQuestionRound == 1);
        } else if (currentQuestionRound == targetQuestionRound) {
            return true;
        }
        if (isIncremented(targetQuestionRound, currentQuestionRound) && !isMaxRoundLimit(maxQuestionRounds)) {
            return true;
        } else if (isIncremented(targetQuestionRound, currentQuestionRound) && targetQuestionRound <= maxQuestionRounds){
            return true;
        }
        return false;
    }

    protected boolean isIncremented(int targetQuestionRound, int currentQuestionRound) {
        return targetQuestionRound == currentQuestionRound + 1;
    }

    protected boolean isMaxRoundLimit(int maxQuestionRounds) {
        return maxQuestionRounds > 0;
    }

    @Transactional
    public List<Question> getQuestionsByQuestionRound(OnlineHearing onlineHearing, int questionRoundNumber){
        return questionRepository.findByOnlineHearingAndQuestionRound(onlineHearing, questionRoundNumber);
    }

    @Transactional
    public List<QuestionRound> getAllQuestionRounds(OnlineHearing onlineHearing){

        List<QuestionRound> questionRounds = new ArrayList<>();

        for(int questionRoundNumber = 1; questionRoundNumber <= getCurrentQuestionRoundNumber(onlineHearing); questionRoundNumber++){
            QuestionRound questionRound = getQuestionRoundByRoundId(onlineHearing, questionRoundNumber);
            questionRounds.add(questionRound);
        }

        return questionRounds;
    }

    @Transactional
    public QuestionRoundState retrieveQuestionRoundState(QuestionRound questionRound) {
        List<Question> questions = questionRound.getQuestionList();
        if (questions.isEmpty()) {
            Optional<QuestionState> optionalDraftedState = questionStateService.retrieveQuestionStateByStateName(DRAFTED);
            if (!optionalDraftedState.isPresent()) {
                throw new NoSuchElementException("Error: Required state not found.");
            }
            return new QuestionRoundState(optionalDraftedState.get());
        }

        // For each QR map answers to questions. JPA multi-bag bug workaround
        questionRound.getQuestionList().forEach(q -> q.setAnswers(answerService.retrieveAnswersByQuestion(q)));

        QuestionRoundState state = new QuestionRoundState();
        if (hasAllQuestionsAnswered(questionRound)) {
            state.setState(QUESTIONS_ANSWERED);
        } else if (hasQuestionRoundAQuestionState(questionRound, QuestionStates.DEADLINE_ELAPSED)) {
            state.setState(QuestionStates.DEADLINE_ELAPSED.getStateName());
        } else if (hasQuestionRoundAQuestionState(questionRound, QuestionStates.QUESTION_DEADLINE_EXTENSION_GRANTED)) {
            state.setState(QuestionStates.QUESTION_DEADLINE_EXTENSION_GRANTED.getStateName());
        } else if (hasQuestionRoundAQuestionState(questionRound, QuestionStates.QUESTION_DEADLINE_EXTENSION_DENIED)) {
            state.setState(QuestionStates.QUESTION_DEADLINE_EXTENSION_DENIED.getStateName());
        } else if (hasQuestionRoundAQuestionState(questionRound, QuestionStates.ISSUED)) {
            state.setState(QuestionStates.ISSUED.getStateName());
        } else {
            state.setState(questions.get(0).getQuestionState());
        }

        return state;
    }

    protected boolean isState(Question question, QuestionState questionState) {
        int questionStateId = question.getQuestionState().getQuestionStateId();
        return questionStateId == questionState.getQuestionStateId();
    }

    protected boolean isState(QuestionRoundState questionRoundState, QuestionState questionState) {
        int questionRoundStateId = questionRoundState.getStateId();
        return questionRoundStateId == questionState.getQuestionStateId();
    }

    @Transactional
    public Integer getCurrentQuestionRoundNumber(OnlineHearing onlineHearing){
        List<Question> orderedQuestions = getQuestionsOrderedByRound(onlineHearing);
        if (orderedQuestions.isEmpty()) {
            return 0;
        }
        return orderedQuestions.get(0).getQuestionRound();
    }

    @Transactional
    public List<Question> getQuestionsOrderedByRound(OnlineHearing onlineHearing) {
        return questionRepository.findAllByOnlineHearingOrderByQuestionRoundDesc(onlineHearing);
    }

    public Integer getNextQuestionRound(OnlineHearing onlineHearing, Integer currentQuestionRound) {
        int maxQuestionRounds = onlineHearing.getJurisdiction().getMaxQuestionRounds();

        if(!isMaxRoundLimit(maxQuestionRounds)){
            return currentQuestionRound + 1;
        }

        if (currentQuestionRound < maxQuestionRounds){
            return currentQuestionRound + 1;
        }else{
            return currentQuestionRound;
        }
    }

    public Integer getPreviousQuestionRound(Integer currentQuestionRound){
        if (currentQuestionRound > 1){
            return currentQuestionRound - 1;
        }
        return currentQuestionRound;
    }

    @Transactional
    public QuestionRound getQuestionRoundByRoundId(OnlineHearing onlineHearing, int roundId) {
        QuestionRound questionRound = new QuestionRound();
        questionRound.setQuestionRoundNumber(roundId);
        questionRound.setQuestionList(getQuestionsByQuestionRound(onlineHearing, roundId));
        questionRound.setQuestionRoundState(retrieveQuestionRoundState(questionRound));

        return questionRound;
    }

    public List<Question> issueQuestionRound(QuestionState questionState, List<Question> questions) {
        List<Question> modifiedQuestion = new ArrayList<>();
        Date expiryDate = ExpiryCalendar.getInstance().getDeadlineExpiryDate();
        questions.forEach(q -> {
            q.setQuestionState(questionState);
            q.updateQuestionStateHistory(questionState);
            q.setDeadlineExpiryDate(expiryDate);
            questionRepository.save(q);
            modifiedQuestion.add(q);
        });

        return modifiedQuestion;
    }

    @Transactional
    public List<Question> issueQuestionRound(OnlineHearing onlineHearing, QuestionState questionState, int questionRoundNumber) {
        List<Question> questions = getQuestionsByQuestionRound(onlineHearing, questionRoundNumber);
        return issueQuestionRound(questionState, questions);
    }

    public boolean hasAllQuestionsAnswered(QuestionRound questionRound) {
        List<Question> questions = questionRound.getQuestionList();
        for (Question question: questions) {
            QuestionState questionState = question.getQuestionState();
            if (!questionState.getState().equals(QuestionStates.ANSWERED.getStateName())) {
                return false;
            }
        }
        return true;
    }

    public boolean hasQuestionRoundAQuestionState(QuestionRound questionRound, QuestionStates state) {
        List<Question> questions = questionRound.getQuestionList();
        for (Question question: questions) {
            QuestionState questionState = question.getQuestionState();
            if (questionState.getState().equals(state.getStateName())) {
                return true;
            }
        }
        return false;
    }
}
