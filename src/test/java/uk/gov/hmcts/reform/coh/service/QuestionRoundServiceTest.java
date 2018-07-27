package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.controller.exceptions.NotAValidUpdateException;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import static junit.framework.TestCase.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class QuestionRoundServiceTest {

    @Spy
    private QuestionRoundService questionRoundService;
    private OnlineHearing onlineHearing;
    private QuestionState draftedState;
    private QuestionState submittedState;
    private QuestionState issuedState;
    private QuestionState issuedPendingState;
    private List<Question> questionRound1Questions;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionStateService questionStateService;


    private static final String draftedStateName = QuestionStates.DRAFTED.getStateName();
    private static final String issuedStateName = QuestionStates.ISSUED.getStateName();
    private static final String issuedPendingStateName = QuestionStates.ISSUED_PENDING.getStateName();

    @Before
    public void setup(){
        draftedState = new QuestionState();
        draftedState.setState(draftedStateName);
        draftedState.setQuestionStateId(1);

        submittedState = new QuestionState();
        submittedState.setQuestionStateId(2);
        submittedState.setState("question_submitted");

        issuedState = new QuestionState();
        issuedState.setQuestionStateId(3);
        issuedState.setState(issuedStateName);

        issuedPendingState = new QuestionState();
        issuedPendingState.setQuestionStateId(3);
        issuedPendingState.setState(issuedPendingStateName);

        List<Question> questions = new ArrayList<>();
        questionRound1Questions = new ArrayList<>();
        Question question = new Question();
        question.setQuestionRound(2);
        questions.add(question);

        question = new Question();
        question.setQuestionRound(1);
        question.setQuestionState(issuedState);
        questionRound1Questions.add(question);

        question = new Question();
        question.setQuestionRound(1);
        questions.add(question);
        question.setQuestionState(issuedState);
        questionRound1Questions.add(question);

        given(questionStateService.retrieveQuestionStateByStateName(issuedStateName)).willReturn(Optional.of(issuedState));
        given(questionStateService.retrieveQuestionStateByStateName(issuedPendingStateName)).willReturn(Optional.of(issuedPendingState));
        given(questionStateService.retrieveQuestionStateByStateName(draftedStateName)).willReturn(Optional.of(draftedState));
        given(questionStateService.retrieveQuestionStateByStateName("question_submitted")).willReturn(Optional.of(submittedState));
        given(questionRepository.findAllByOnlineHearingOrderByQuestionRoundDesc(any(OnlineHearing.class))).willReturn(questions);
        given(questionRepository.findByOnlineHearingAndQuestionRound(any(OnlineHearing.class), anyInt())).willReturn(questionRound1Questions);
        QuestionRoundService questionRoundServiceImpl = new QuestionRoundService(questionRepository, questionStateService);
        questionRoundService = spy(questionRoundServiceImpl);

        onlineHearing = new OnlineHearing();
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setMaxQuestionRounds(3);
        onlineHearing.setJurisdiction(jurisdiction);
    }

    @Test
    public void testFindQuestionsOfOnlineHearingOrderedByQuestionRoundReturnsPopulatedList(){
        List<Question> questions = questionRoundService.getQuestionsOrderedByRound(new OnlineHearing());
        assertFalse(questions.isEmpty());
    }

    @Test
    public void testGetQuestionRoundReturnsHighestNumberFromQuestionList(){
        int questionRound = questionRoundService.getCurrentQuestionRoundNumber(new OnlineHearing());
        assertEquals(2, questionRound);
    }

    @Test
    public void testQuestionRoundMustBeZeroIfNoOtherQuestionsAreFound(){
        Question question = new Question();
        question.setQuestionRound(3);

        given(questionRepository.findAllByOnlineHearingOrderByQuestionRoundDesc(any(OnlineHearing.class))).willReturn(new ArrayList<>());
        assertFalse(questionRoundService.isQrValidTransition(question, onlineHearing));
    }

    @Test
    public void testGetQuestionRoundReturns1IfNoPreviousQuestionsFound(){
        given(questionRepository.findAllByOnlineHearingOrderByQuestionRoundDesc(any(OnlineHearing.class))).willReturn(new ArrayList<>());
        int questionRound = questionRoundService.getCurrentQuestionRoundNumber(new OnlineHearing());
        assertEquals(0, questionRound);
    }


    @Test
    public void testIsIncrementedReturnsTrueWhenOneToTwo(){
        assertTrue(questionRoundService.isIncremented(2, 1));
    }

    @Test
    public void testIsIncrementedReturnsFalseWhenOneToThree(){
        assertFalse(questionRoundService.isIncremented(3, 1));
    }

    @Test
    public void testIsIncrementedReturnsFalseWhenTwoToOne(){
        assertFalse(questionRoundService.isIncremented(1, 2));
    }

    @Test
    public void testIsMaxRoundLimitIsTrueIfValueSetAboveZero() {
        assertTrue(questionRoundService.isMaxRoundLimit(1));
    }

    @Test
    public void testIsMaxRoundLimitIsFalseIfValueSetToZero() {
        assertFalse(questionRoundService.isMaxRoundLimit(0));
    }

    @Test
    public void testValidateQuestionsRoundWhenNoJurisdictionLimitSet(){
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setMaxQuestionRounds(0);
        onlineHearing.setJurisdiction(jurisdiction);

        Question question = new Question();
        question.setQuestionRound(3);
        assertTrue(questionRoundService.isQrValidTransition(question, onlineHearing));
    }

    @Test
    public void testValidateQuestionRoundHappyPath(){
        Question question = new Question();
        question.setQuestionRound(3);
        assertTrue(questionRoundService.isQrValidTransition(question, onlineHearing));
    }

    @Test
    public void testIfCurrentQuestionRoundIsZeroThenQuestionRoundMustBeOne(){
        List<Question> questions = new ArrayList<>();

        Question question = new Question();
        question.setQuestionRound(1);

        given(questionRepository.findAllByOnlineHearingOrderByQuestionRoundDesc(any(OnlineHearing.class))).willReturn(questions);
        assertTrue(questionRoundService.isQrValidTransition(question, onlineHearing));
    }

    @Test
    public void testIfCurrentQuestionRoundIsZeroThenQuestionRoundCannotBeTwo(){
        List<Question> questions = new ArrayList<>();

        Question question = new Question();
        question.setQuestionRound(2);

        given(questionRepository.findAllByOnlineHearingOrderByQuestionRoundDesc(any(OnlineHearing.class))).willReturn(questions);
        assertFalse(questionRoundService.isQrValidTransition(question, onlineHearing));
    }

    @Test
    public void testValidateQuestionRoundFailsWhenExceedingMaxQuestionRounds(){
        Question question = new Question();
        question.setQuestionRound(4);
        assertFalse(questionRoundService.isQrValidTransition(question, onlineHearing));
    }

    @Test
    public void testValidateQuestionRoundIsTrueWhenIncrementedByOne(){
        Question question = new Question();
        question.setQuestionRound(3);
        assertTrue(questionRoundService.isQrValidTransition(question, onlineHearing));
    }

    @Test
    public void testValidateQuestionRoundIsFalseWhenSettingToPreviousQuestionRound(){
        Question question = new Question();
        question.setQuestionRound(1);
        assertFalse(questionRoundService.isQrValidTransition(question, onlineHearing));
    }

    @Test
    public void testValidateQuestionRoundAcceptsSameQuestionRound(){
        Question question = new Question();
        question.setQuestionRound(2);
        assertTrue(questionRoundService.isQrValidTransition(question, onlineHearing));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testValidateQuestionRoundThrowsErrorIfQuestionRoundIs0(){
        Question question = new Question();
        question.setQuestionRound(0);
        questionRoundService.isQrValidTransition(question, onlineHearing);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testValidateQuestionRoundThrowsErrorIfQuestionRoundIsNull(){
        questionRoundService.isQrValidTransition(new Question(), onlineHearing);
    }

    @Test
    public void testQuestionIsStateDraftedIsTrueWhenStateIsDrafted(){
        Question question = new Question();
        question.setQuestionState(draftedState);
        assertTrue(questionRoundService.isState(question, draftedState));
    }

    @Test
    public void testQuestionIsStateFalseSubmittedIsTrueWhenStateIsDrafted(){
        Question question = new Question();
        question.setQuestionState(submittedState);
        assertFalse(questionRoundService.isState(question, draftedState));
    }

    @Test
    public void testQuestionRoundIsStateDraftedIsTrueWhenStateIsDrafted(){
        QuestionRoundState questionRoundState = new QuestionRoundState();
        questionRoundState.setState(draftedState);
        assertTrue(questionRoundService.isState(questionRoundState, draftedState));
    }

    @Test
    public void testQuestionRoundIsStateFalseSubmittedIsTrueWhenStateIsDrafted(){
        QuestionRoundState questionRoundState = new QuestionRoundState();
        questionRoundState.setState(submittedState);
        assertFalse(questionRoundService.isState(questionRoundState, draftedState));
    }

    @Test
    public void testGetNextQuestionRoundReturnsIncrementedNumber(){
        int nextQuestionRound = questionRoundService.getNextQuestionRound(onlineHearing, 2);
        assertEquals(3, nextQuestionRound);
    }

    @Test
    public void testGetNextQuestionRoundReturnsMaxNumberIfAtLimit(){
        int nextQuestionRound = questionRoundService.getNextQuestionRound(onlineHearing, 3);
        assertEquals(3, nextQuestionRound);
    }

    @Test
    public void testGetNextQuestionRoundReturnsIncrementNumberIfNoLimitSet(){
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setMaxQuestionRounds(0);
        onlineHearing.setJurisdiction(jurisdiction);
        int nextQuestionRound = questionRoundService.getNextQuestionRound(onlineHearing, 3);
        assertEquals(4, nextQuestionRound);
    }

    @Test
    public void testGetPreviousQuestionRoundReturnsPreviousNumber(){
        int previousQuestionRound = questionRoundService.getPreviousQuestionRound(2);
        assertEquals(1, previousQuestionRound);
    }

    @Test
    public void testGetPreviousQuestionRoundReturns1IfCurrentIs1(){
        int previousQuestionRound = questionRoundService.getPreviousQuestionRound(1);
        assertEquals(1, previousQuestionRound);
    }

    @Test
    public void testGetAllQuestionRoundsOfOnlineHearing(){
        List<QuestionRound> questionRounds = questionRoundService.getAllQuestionRounds(onlineHearing);
        assertEquals(2, questionRounds.size());
    }

    @Test
    public void testGetAllQuestionsByRoundId(){
        List<Question> roundOneQuestions = questionRoundService.getQuestionsByQuestionRound(onlineHearing, 1);
        assertEquals(2, roundOneQuestions.size());
    }

    @Test(expected = NoSuchElementException.class)
    public void testRetrieveQuestionRoundThrowsNoSuchElementIfCannotFindDraftedState() {
        given(questionStateService.retrieveQuestionStateByStateName(anyString())).willReturn(Optional.empty());
        QuestionRound questionRound = new QuestionRound();
        questionRound.setQuestionList(new ArrayList<>());
        assertEquals(draftedState.getState(), questionRoundService.retrieveQuestionRoundState(questionRound));
    }

    @Test
    public void testRetrieveQuestionRoundReturnsDraftedIfNoQuestionsExist() {
        QuestionRound questionRound = new QuestionRound();
        questionRound.setQuestionList(new ArrayList<>());
        assertEquals(draftedState.getState(), questionRoundService.retrieveQuestionRoundState(questionRound).getState());
    }

    @Test
    public void testRetrieveQuestionRoundStateReturnsStateIfAllQuestionStatesAreIssued() {
        QuestionRound questionRound = new QuestionRound();
        questionRound.setQuestionList(questionRound1Questions);
        assertEquals(issuedState.getState(), questionRoundService.retrieveQuestionRoundState(questionRound).getState());
    }

    @Test
    public void testIssueQuestionRound() {
        doReturn(new QuestionRoundState(draftedState)).when(questionRoundService).retrieveQuestionRoundState(any(QuestionRound.class));
        List<Question> questions = questionRoundService.issueQuestionRound(onlineHearing, issuedState, 1);
        assertEquals(2, questions.size());
        questions.stream().forEach(q -> assertEquals(issuedState.getState(), q.getQuestionState().getState()));
    }

    @Test
    public void testIncrementQrWhenNotIssuedInvalid() {
        Question question = new Question();
        question.setQuestionRound(2);
        question.setQuestionState(draftedState);

        doReturn(2).when(questionRoundService).getCurrentQuestionRoundNumber(any(OnlineHearing.class));
        doReturn(new QuestionRoundState(issuedState)).when(questionRoundService).retrieveQuestionRoundState(any(QuestionRound.class));

        assertFalse(questionRoundService.isQrValidState(question, onlineHearing));
    }

    @Test
    public void testIncrementQrWhenNotIssuedPendingInvalid() {
        Question question = new Question();
        question.setQuestionRound(2);
        question.setQuestionState(draftedState);

        doReturn(2).when(questionRoundService).getCurrentQuestionRoundNumber(any(OnlineHearing.class));
        doReturn(new QuestionRoundState(issuedPendingState)).when(questionRoundService).retrieveQuestionRoundState(any(QuestionRound.class));

        assertFalse(questionRoundService.isQrValidState(question, onlineHearing));
    }

    @Test
    public void testIncrementQrWhenIssuedIsValid() {
        Question question = new Question();
        question.setQuestionRound(2);
        question.setQuestionState(draftedState);

        doReturn(1).when(questionRoundService).getCurrentQuestionRoundNumber(any(OnlineHearing.class));
        doReturn(new QuestionRoundState(issuedState)).when(questionRoundService).retrieveQuestionRoundState(any(QuestionRound.class));

        assertTrue(questionRoundService.isQrValidState(question, onlineHearing));
    }

    @Test
    public void testIncrementQrWhenIssuedPendingIsValid() {
        Question question = new Question();
        question.setQuestionRound(2);
        question.setQuestionState(draftedState);

        doReturn(1).when(questionRoundService).getCurrentQuestionRoundNumber(any(OnlineHearing.class));
        doReturn(new QuestionRoundState(issuedPendingState)).when(questionRoundService).retrieveQuestionRoundState(any(QuestionRound.class));

        assertTrue(questionRoundService.isQrValidState(question, onlineHearing));
    }

    @Test
    public void testAddQuestionToCurrentQrWhenNotIssuedIsValid() {
        Question question = new Question();
        question.setQuestionRound(1);
        question.setQuestionState(draftedState);

        doReturn(1).when(questionRoundService).getCurrentQuestionRoundNumber(any(OnlineHearing.class));
        doReturn(new QuestionRoundState(draftedState)).when(questionRoundService).retrieveQuestionRoundState(any(QuestionRound.class));

        assertTrue(questionRoundService.isQrValidState(question, onlineHearing));
    }

    @Test
    public void testAddQuestionToCurrentQrWhenIssuedIsInvalid() {
        Question question = new Question();
        question.setQuestionRound(1);
        question.setQuestionState(draftedState);

        doReturn(1).when(questionRoundService).getCurrentQuestionRoundNumber(any(OnlineHearing.class));
        doReturn(new QuestionRoundState(issuedState)).when(questionRoundService).retrieveQuestionRoundState(any(QuestionRound.class));

        assertFalse(questionRoundService.isQrValidState(question, onlineHearing));
    }

    @Test
    public void testAddQuestionToCurrentQrWhenIssuedPendingIsInvalid() {
        Question question = new Question();
        question.setQuestionRound(1);
        question.setQuestionState(draftedState);

        doReturn(1).when(questionRoundService).getCurrentQuestionRoundNumber(any(OnlineHearing.class));
        doReturn(new QuestionRoundState(issuedPendingState)).when(questionRoundService).retrieveQuestionRoundState(any(QuestionRound.class));

        assertFalse(questionRoundService.isQrValidState(question, onlineHearing));
    }

    @Test
    public void testIssueQuestionRoundChangesAllQuestionStatesAndSavesToDb() {
        List<Question> questions = new ArrayList<>();
        questions.add(new Question());
        questions.add(new Question());
        questions.add(new Question());

        given(questionRepository.findByOnlineHearingAndQuestionRound(any(OnlineHearing.class), anyInt())).willReturn(questions);
        doReturn(new QuestionRoundState(draftedState)).when(questionRoundService).retrieveQuestionRoundState(any(QuestionRound.class));
        questionRoundService.issueQuestionRound(onlineHearing, issuedState, 1);
        List<Question> issuedQuestions = questions.stream()
                .filter(q -> q.getQuestionState().getState().equals(issuedStateName))
                .collect(Collectors.toList());
        assertEquals(3, issuedQuestions.size());
        verify(questionRepository, times(3)).save(any(Question.class));
        assertTrue(issuedQuestions.stream().allMatch(q -> q.getDeadlineExpiryDate() != null));
    }

    @Test(expected = NotAValidUpdateException.class)
    public void testReissuingTheCurrentQuestionThrowsNotAValidUpdate() {
        doReturn(new QuestionRoundState(issuedPendingState)).when(questionRoundService).retrieveQuestionRoundState(any(QuestionRound.class));

        questionRoundService.issueQuestionRound(onlineHearing, issuedPendingState, 1);
    }

    @Test(expected = NoSuchElementException.class)
    public void testIssuedStateNotFoundThrowsException() {
        Question question = new Question();
        question.setQuestionRound(1);
        question.setQuestionState(draftedState);

        given(questionStateService.retrieveQuestionStateByStateName(issuedStateName)).willReturn(Optional.empty());
        questionRoundService.isQrValidState(question, onlineHearing);
    }

    @Test(expected = NoSuchElementException.class)
    public void testIsusedPendingStateNotFoundThrowsException() {
        Question question = new Question();
        question.setQuestionRound(1);
        question.setQuestionState(draftedState);

        given(questionStateService.retrieveQuestionStateByStateName(issuedPendingStateName)).willReturn(Optional.empty());
        questionRoundService.isQrValidState(question, onlineHearing);
    }
}

