package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;
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
    private List<Question> questionRound1Questions;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionStateService questionStateService;


    private static final String draftedStateName = QuestionStates.DRAFTED.getStateName();
    private static final String issuedStateName = QuestionStates.ISSUED.getStateName();

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
        boolean valid = questionRoundService.isQrValidTransition(question, onlineHearing);
        assertFalse(valid);
    }

    @Test
    public void testGetQuestionRoundReturns1IfNoPreviousQuestionsFound(){
        given(questionRepository.findAllByOnlineHearingOrderByQuestionRoundDesc(any(OnlineHearing.class))).willReturn(new ArrayList<>());
        int questionRound = questionRoundService.getCurrentQuestionRoundNumber(new OnlineHearing());
        assertEquals(0, questionRound);
    }


    @Test
    public void testIsIncrementedReturnsTrueWhenOneToTwo(){
        boolean valid = questionRoundService.isIncremented(2, 1);
        assertTrue(valid);
    }

    @Test
    public void testIsIncrementedReturnsFalseWhenOneToThree(){
        boolean valid = questionRoundService.isIncremented(3, 1);
        assertFalse(valid);
    }

    @Test
    public void testIsIncrementedReturnsFalseWhenTwoToOne(){
        boolean valid = questionRoundService.isIncremented(1, 2);
        assertFalse(valid);
    }

    @Test
    public void testIsMaxRoundLimitIsTrueIfValueSetAboveZero() {
        boolean valid = questionRoundService.isMaxRoundLimit(1);
        assertTrue(valid);
    }

    @Test
    public void testIsMaxRoundLimitIsFalseIfValueSetToZero() {
        boolean valid = questionRoundService.isMaxRoundLimit(0);
        assertFalse(valid);
    }

    @Test
    public void testValidateQuestionsRoundWhenNoJurisdictionLimitSet(){
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setMaxQuestionRounds(0);
        onlineHearing.setJurisdiction(jurisdiction);

        Question question = new Question();
        question.setQuestionRound(3);
        boolean valid = questionRoundService.isQrValidTransition(question, onlineHearing);
        assertTrue(valid);
    }

    @Test
    public void testValidateQuestionRoundHappyPath(){
        Question question = new Question();
        question.setQuestionRound(3);
        boolean valid = questionRoundService.isQrValidTransition(question, onlineHearing);
        assertTrue(valid);
    }

    @Test
    public void testIfCurrentQuestionRoundIsZeroThenQuestionRoundMustBeOne(){
        List<Question> questions = new ArrayList<>();

        Question question = new Question();
        question.setQuestionRound(1);

        given(questionRepository.findAllByOnlineHearingOrderByQuestionRoundDesc(any(OnlineHearing.class))).willReturn(questions);
        boolean valid = questionRoundService.isQrValidTransition(question, onlineHearing);
        assertTrue(valid);
    }

    @Test
    public void testIfCurrentQuestionRoundIsZeroThenQuestionRoundCannotBeTwo(){
        List<Question> questions = new ArrayList<>();

        Question question = new Question();
        question.setQuestionRound(2);

        given(questionRepository.findAllByOnlineHearingOrderByQuestionRoundDesc(any(OnlineHearing.class))).willReturn(questions);
        boolean valid = questionRoundService.isQrValidTransition(question, onlineHearing);
        assertFalse(valid);
    }

    @Test
    public void testValidateQuestionRoundFailsWhenExceedingMaxQuestionRounds(){
        Question question = new Question();
        question.setQuestionRound(4);
        boolean valid = questionRoundService.isQrValidTransition(question, onlineHearing);
        assertFalse(valid);
    }

    @Test
    public void testValidateQuestionRoundIsTrueWhenIncrementedByOne(){
        Question question = new Question();
        question.setQuestionRound(3);
        boolean valid = questionRoundService.isQrValidTransition(question, onlineHearing);
        assertTrue(valid);
    }

    @Test
    public void testValidateQuestionRoundIsFalseWhenSettingToPreviousQuestionRound(){
        Question question = new Question();
        question.setQuestionRound(1);
        boolean valid = questionRoundService.isQrValidTransition(question, onlineHearing);
        assertFalse(valid);
    }

    @Test
    public void testValidateQuestionRoundAcceptsSameQuestionRound(){
        Question question = new Question();
        question.setQuestionRound(2);
        boolean valid = questionRoundService.isQrValidTransition(question, onlineHearing);
        assertTrue(valid);
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
        boolean valid = questionRoundService.isState(question, draftedState);
        assertTrue(valid);
    }

    @Test
    public void testQuestionIsStateFalseSubmittedIsTrueWhenStateIsDrafted(){
        Question question = new Question();
        question.setQuestionState(submittedState);
        boolean valid = questionRoundService.isState(question, draftedState);
        assertFalse(valid);
    }

    @Test
    public void testQuestionRoundIsStateDraftedIsTrueWhenStateIsDrafted(){
        QuestionRoundState questionRoundState = new QuestionRoundState();
        questionRoundState.setState(draftedState);
        boolean valid = questionRoundService.isState(questionRoundState, draftedState);
        assertTrue(valid);
    }

    @Test
    public void testQuestionRoundIsStateFalseSubmittedIsTrueWhenStateIsDrafted(){
        QuestionRoundState questionRoundState = new QuestionRoundState();
        questionRoundState.setState(submittedState);
        boolean valid = questionRoundService.isState(questionRoundState, draftedState);
        assertFalse(valid);
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
        QuestionRoundState questionRoundState = questionRoundService.retrieveQuestionRoundState(questionRound);
        assertEquals( draftedState.getState(), questionRoundState.getState());
    }

    @Test
    public void testRetrieveQuestionRoundReturnsDraftedIfNoQuestionsExist() {
        QuestionRound questionRound = new QuestionRound();
        questionRound.setQuestionList(new ArrayList<>());
        QuestionRoundState questionRoundState = questionRoundService.retrieveQuestionRoundState(questionRound);
        assertEquals( draftedState.getState(), questionRoundState.getState());
    }

    @Test
    public void testRetrieveQuestionRoundStateReturnsStateIfAllQuestionStatesAreIssued() {
        QuestionRound questionRound = new QuestionRound();
        questionRound.setQuestionList(questionRound1Questions);
        QuestionRoundState questionRoundState = questionRoundService.retrieveQuestionRoundState(questionRound);
        assertEquals( issuedState.getState(), questionRoundState.getState());
    }

    @Test
    public void testIssueQuestionRound() {
        List<Question> questions = questionRoundService.issueQuestionRound(onlineHearing, issuedState, 1);
        assertEquals(2, questions.size());
        questions.stream().forEach(q -> assertEquals("question_issued", q.getQuestionState().getState()));
    }

    @Test
    public void testIncrementQrWhenNotIssuedInvalid() {
        Question question = new Question();
        question.setQuestionRound(2);
        question.setQuestionState(draftedState);

        doReturn(2).when(questionRoundService).getCurrentQuestionRoundNumber(any(OnlineHearing.class));

        QuestionRoundState issuedQrState = new QuestionRoundState(issuedState);
        doReturn(issuedQrState).when(questionRoundService).retrieveQuestionRoundState(any(QuestionRound.class));

        boolean isValid = questionRoundService.isQrValidState(question, onlineHearing);
        assertFalse(isValid);
    }

    @Test
    public void testIncrementQrWhenIssuedIsValid() {
        Question question = new Question();
        question.setQuestionRound(2);
        question.setQuestionState(draftedState);

        doReturn(1).when(questionRoundService).getCurrentQuestionRoundNumber(any(OnlineHearing.class));

        QuestionRoundState issuedQrState = new QuestionRoundState(issuedState);
        doReturn(issuedQrState).when(questionRoundService).retrieveQuestionRoundState(any(QuestionRound.class));

        boolean isValid = questionRoundService.isQrValidState(question, onlineHearing);
        assertTrue(isValid);
    }

    @Test
    public void testAddQuestionToCurrentQrWhenNotIssuedIsValid() {
        Question question = new Question();
        question.setQuestionRound(1);
        question.setQuestionState(draftedState);

        doReturn(1).when(questionRoundService).getCurrentQuestionRoundNumber(any(OnlineHearing.class));
        QuestionRoundState draftedQrState = new QuestionRoundState(draftedState);
        doReturn(draftedQrState).when(questionRoundService).retrieveQuestionRoundState(any(QuestionRound.class));

        boolean isValid = questionRoundService.isQrValidState(question, onlineHearing);
        assertTrue(isValid);
    }

    @Test
    public void testAddQuestionToCurrentQrWhenIssuedIsInvalid() {
        Question question = new Question();
        question.setQuestionRound(1);
        question.setQuestionState(draftedState);

        doReturn(1).when(questionRoundService).getCurrentQuestionRoundNumber(any(OnlineHearing.class));
        QuestionRoundState issuedQrState = new QuestionRoundState(issuedState);
        doReturn(issuedQrState).when(questionRoundService).retrieveQuestionRoundState(any(QuestionRound.class));

        boolean isValid = questionRoundService.isQrValidState(question, onlineHearing);
        assertFalse(isValid);
    }

    @Test
    public void testIssueQuestionRoundChangesAllQuestionStatesAndSavesToDb() {
        List<Question> questions = new ArrayList<>();
        questions.add(new Question());
        questions.add(new Question());
        questions.add(new Question());

        given(questionRepository.findByOnlineHearingAndQuestionRound(any(OnlineHearing.class), anyInt())).willReturn(questions);

        questionRoundService.issueQuestionRound(onlineHearing, issuedState, 1);
        List<Question> issuedQuestions = questions.stream()
                .filter(q -> q.getQuestionState().getState().equals(issuedStateName))
                .collect(Collectors.toList());
        assertEquals(3, issuedQuestions.size());
        verify(questionRepository, times(3)).save(any(Question.class));
    }
}

