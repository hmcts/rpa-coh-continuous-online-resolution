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
    public static final String DRAFTED = QuestionStates.DRAFTED.getStateName();
    public static final String ISSUED_PENDING = QuestionStates.ISSUE_PENDING.getStateName();
    public static final String ISSUED = QuestionStates.ISSUED.getStateName();

    public QuestionRoundService() {}

    @Autowired
    public QuestionRoundService(QuestionRepository questionRepository, QuestionStateService questionStateService) {
        this.questionRepository = questionRepository;
        this.questionStateService = questionStateService;
    }

    public boolean isQrValidState(Question question, OnlineHearing onlineHearing) {
        int targetQuestionRound = question.getQuestionRound();
        int currentRoundNumber = getCurrentQuestionRoundNumber(onlineHearing);

        Optional<QuestionState> optionalIssuedState = questionStateService.retrieveQuestionStateByStateName(ISSUED);
        if(!optionalIssuedState.isPresent()){
            throw new NoSuchElementException("Error: Required state not found.");
        }

        Optional<QuestionState> optionalIssuePendingState = questionStateService.retrieveQuestionStateByStateName(ISSUED_PENDING);
        if(!optionalIssuePendingState.isPresent()){
            throw new NoSuchElementException("Error: Required state not found.");
        }
        QuestionRoundState issuedState = new QuestionRoundState(optionalIssuedState.get());
        QuestionRoundState issuedPendingState = new QuestionRoundState(optionalIssuePendingState.get());
        QuestionRoundState currentState = retrieveQuestionRoundState(getQuestionRoundByRoundId(onlineHearing, currentRoundNumber));

        // Current QR is issued and create new question round
        if(currentState.equals(issuedState) && isIncremented(targetQuestionRound, currentRoundNumber)
            || currentState.equals(issuedPendingState) && isIncremented(targetQuestionRound, currentRoundNumber)) {
            return true;
        }

        // Current QR is not issued and question is current question round OR no QR exists yet
        if(!currentState.equals(issuedState) && !currentState.equals(issuedPendingState)
                && targetQuestionRound == currentRoundNumber || currentRoundNumber == 0) {
            return true;
        }

        return false;
    }

    public boolean isQrValidTransition(Question question, OnlineHearing onlineHearing) {
        if (question.getQuestionRound() == null || question.getQuestionRound() == 0) {
            throw new EntityNotFoundException();
        }
        Jurisdiction jurisdiction = onlineHearing.getJurisdiction();

        int maxQuestionRounds = jurisdiction.getMaxQuestionRounds();
        int targetQuestionRound = question.getQuestionRound();
        int currentQuestionRound = getCurrentQuestionRoundNumber(onlineHearing);

        if (currentQuestionRound == 0) {
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
        if(questions.isEmpty()) {
            Optional<QuestionState> optionalDraftedState = questionStateService.retrieveQuestionStateByStateName(DRAFTED);
            if(!optionalDraftedState.isPresent()) {
                throw new NoSuchElementException("Error: Required state not found.");
            }
            return new QuestionRoundState(optionalDraftedState.get());
        }
        return new QuestionRoundState(questions.get(0).getQuestionState());
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

    @Transactional
    public List<Question> issueQuestionRound(OnlineHearing onlineHearing, QuestionState questionState, int questionRoundNumber) {
        List<Question> modifiedQuestion = new ArrayList<>();
        List<Question> questions = getQuestionsByQuestionRound(onlineHearing, questionRoundNumber);
        QuestionRoundState qrState = retrieveQuestionRoundState(getQuestionRoundByRoundId(onlineHearing, questionRoundNumber));

        if(qrState.getState().equals(QuestionStates.ISSUE_PENDING.getStateName()) ||
                qrState.getState().equals(QuestionStates.ISSUED.getStateName())){
            throw new NotAValidUpdateException("Question round has already been issued");
        }

        Date expiryDate = ExpiryCalendar.getDeadlineExpiryDate();
        questions.stream().forEach(q -> {
            q.setQuestionState(questionState);
            q.updateQuestionStateHistory(questionState);
            q.setDeadlineExpiryDate(expiryDate);
            questionRepository.save(q);
            modifiedQuestion.add(q);
        });

        return modifiedQuestion;
    }
}
