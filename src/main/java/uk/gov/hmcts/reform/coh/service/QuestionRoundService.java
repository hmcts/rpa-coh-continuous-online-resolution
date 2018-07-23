package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.controller.exceptions.NotAValidUpdateException;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;
import uk.gov.hmcts.reform.coh.service.utils.ExpiryCalendar;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import javax.persistence.EntityNotFoundException;
import java.util.*;

@Service
@Component
public class QuestionRoundService {

    private QuestionRepository questionRepository;
    private QuestionStateService questionStateService;
    private NotificationService notificationService;

    public static final String DRAFTED = QuestionStates.DRAFTED.getStateName();
    public static final String ISSUED = QuestionStates.ISSUED.getStateName();

    public QuestionRoundService() {}

    @Autowired
    public QuestionRoundService(QuestionRepository questionRepository, QuestionStateService questionStateService, NotificationService notificationService) {
        this.questionRepository = questionRepository;
        this.questionStateService = questionStateService;
        this.notificationService = notificationService;
    }

    public boolean isQrValidState(Question question, OnlineHearing onlineHearing) {
        int targetQuestionRound = question.getQuestionRound();
        int currentRoundNumber = getCurrentQuestionRoundNumber(onlineHearing);

        Optional<QuestionState> optionalQuestionState = questionStateService.retrieveQuestionStateByStateName(ISSUED);
        if(!optionalQuestionState.isPresent()){
            throw new NoSuchElementException("Error: Required state not found.");
        }

        QuestionRoundState issuedQrState = new QuestionRoundState(optionalQuestionState.get());

        QuestionRoundState currentQrState = retrieveQuestionRoundState(getQuestionRoundByRoundId(onlineHearing, currentRoundNumber));
        // Current QR is issued and create new question round
        if(currentQrState.equals(issuedQrState) && isIncremented(targetQuestionRound, currentRoundNumber)) {
            return true;
        }

        // Current QR is not issued and question is current question round OR no QR exists yet
        if(!currentQrState.equals(issuedQrState) && targetQuestionRound == currentRoundNumber || currentRoundNumber == 0) {
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

    public List<Question> getQuestionsByQuestionRound(OnlineHearing onlineHearing, int questionRoundNumber){
        return questionRepository.findByOnlineHearingAndQuestionRound(onlineHearing, questionRoundNumber);
    }

    public List<QuestionRound> getAllQuestionRounds(OnlineHearing onlineHearing){

        List<QuestionRound> questionRounds = new ArrayList<>();

        for(int questionRoundNumber = 1; questionRoundNumber <= getCurrentQuestionRoundNumber(onlineHearing); questionRoundNumber++){
            QuestionRound questionRound = getQuestionRoundByRoundId(onlineHearing, questionRoundNumber);
            questionRounds.add(questionRound);
        }

        return questionRounds;
    }

    protected QuestionRoundState retrieveQuestionRoundState(QuestionRound questionRound) {
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

    public Integer getCurrentQuestionRoundNumber(OnlineHearing onlineHearing){
        List<Question> orderedQuestions = getQuestionsOrderedByRound(onlineHearing);
        if (orderedQuestions.isEmpty()) {
            return 0;
        }
        return orderedQuestions.get(0).getQuestionRound();
    }

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

    public QuestionRound getQuestionRoundByRoundId(OnlineHearing onlineHearing, int roundId) {
        QuestionRound questionRound = new QuestionRound();
        questionRound.setQuestionRoundNumber(roundId);
        questionRound.setQuestionList(getQuestionsByQuestionRound(onlineHearing, roundId));
        questionRound.setQuestionRoundState(retrieveQuestionRoundState(questionRound));

        return questionRound;
    }

    public List<Question> issueQuestionRound(OnlineHearing onlineHearing, QuestionState questionState, int questionRoundNumber) {
        List<Question> modifiedQuestion = new ArrayList<>();
        List<Question> questions = getQuestionsByQuestionRound(onlineHearing, questionRoundNumber);
        QuestionRoundState qrState = retrieveQuestionRoundState(getQuestionRoundByRoundId(onlineHearing, questionRoundNumber));

        if(qrState.getState().equals(QuestionStates.ISSUED.getStateName())){
            throw new NotAValidUpdateException("Question round has already been issued");
        }

        boolean success = notificationService.notifyIssuedQuestionRound(onlineHearing);
        if(success) {
            Date expiryDate = ExpiryCalendar.getDeadlineExpiryDate();
            questions.stream().forEach(q -> {
                q.setQuestionState(questionState);
                q.updateQuestionStateHistory(questionState);
                q.setDeadlineExpiryDate(expiryDate);
                questionRepository.save(q);
                modifiedQuestion.add(q);
            });
        }
        return modifiedQuestion;
    }
}
