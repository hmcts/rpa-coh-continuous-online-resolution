package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class QuestionRoundServiceTest {

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

    @Before
    public void setup(){
        draftedState = new QuestionState();
        draftedState.setState("DRAFTED");
        draftedState.setQuestionStateId(1);

        submittedState = new QuestionState();
        submittedState.setQuestionStateId(2);
        submittedState.setState("SUBMITTED");

        issuedState = new QuestionState();
        issuedState.setQuestionStateId(3);
        issuedState.setState("ISSUED");

        given(questionStateService.retrieveQuestionStateById(1)).willReturn(draftedState);
        given(questionStateService.retrieveQuestionStateById(2)).willReturn(submittedState);
        given(questionStateService.retrieveQuestionStateById(3)).willReturn(issuedState);

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

        given(questionRepository.findAllByOnlineHearingOrderByQuestionRoundDesc(any(OnlineHearing.class))).willReturn(questions);
        given(questionRepository.findByOnlineHearingAndQuestionRound(any(OnlineHearing.class), anyInt())).willReturn(questionRound1Questions);
        questionRoundService = new QuestionRoundService(questionRepository, questionStateService);

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
        boolean valid = questionRoundService.validateQuestionRound(question, onlineHearing);
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
        boolean valid = questionRoundService.validateQuestionRound(question, onlineHearing);
        assertTrue(valid);
    }

    @Test
    public void testValidateQuestionRoundHappyPath(){
        Question question = new Question();
        question.setQuestionRound(3);
        boolean valid = questionRoundService.validateQuestionRound(question, onlineHearing);
        assertTrue(valid);
    }

    @Test
    public void testIfCurrentQuestionRoundIsZeroThenQuestionRoundMustBeOne(){
        List<Question> questions = new ArrayList<>();

        Question question = new Question();
        question.setQuestionRound(1);

        given(questionRepository.findAllByOnlineHearingOrderByQuestionRoundDesc(any(OnlineHearing.class))).willReturn(questions);
        boolean valid = questionRoundService.validateQuestionRound(question, onlineHearing);
        assertTrue(valid);
    }

    @Test
    public void testIfCurrentQuestionRoundIsZeroThenQuestionRoundCannotBeTwo(){
        List<Question> questions = new ArrayList<>();

        Question question = new Question();
        question.setQuestionRound(2);

        given(questionRepository.findAllByOnlineHearingOrderByQuestionRoundDesc(any(OnlineHearing.class))).willReturn(questions);
        boolean valid = questionRoundService.validateQuestionRound(question, onlineHearing);
        assertFalse(valid);
    }
    @Test
    public void testValidateQuestionRoundFailsWhenExceedingMaxQuestionRounds(){
        Question question = new Question();
        question.setQuestionRound(4);
        boolean valid = questionRoundService.validateQuestionRound(question, onlineHearing);
        assertFalse(valid);
    }

    @Test
    public void testValidateQuestionRoundIsTrueWhenIncrementedByOne(){
        Question question = new Question();
        question.setQuestionRound(3);
        boolean valid = questionRoundService.validateQuestionRound(question, onlineHearing);
        assertTrue(valid);
    }

    @Test
    public void testValidateQuestionRoundIsFalseWhenSettingToPreviousQuestionRound(){
        Question question = new Question();
        question.setQuestionRound(1);
        boolean valid = questionRoundService.validateQuestionRound(question, onlineHearing);
        assertFalse(valid);
    }

    @Test
    public void testValidateQuestionRoundAcceptsSameQuestionRound(){
        Question question = new Question();
        question.setQuestionRound(2);
        boolean valid = questionRoundService.validateQuestionRound(question, onlineHearing);
        assertTrue(valid);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testValidateQuestionRoundThrowsErrorIfQuestionRoundIs0(){
        Question question = new Question();
        question.setQuestionRound(0);
        questionRoundService.validateQuestionRound(question, onlineHearing);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testValidateQuestionRoundThrowsErrorIfQuestionRoundIsNull(){
        questionRoundService.validateQuestionRound(new Question(), onlineHearing);
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

    @Test
    public void testRetrieveQuestionRoundStateReturnsStateIfAllQuestionStatesAreIssued() {
        QuestionRound questionRound = new QuestionRound();
        questionRound.setQuestionList(questionRound1Questions);
        QuestionRoundState questionRoundState = questionRoundService.retrieveQuestionRoundState(questionRound);
        assertEquals( issuedState.getState(), questionRoundState.getState());
    }

    @Test
    public void testRetrieveQuestionRoundStateGetsTheNextLowestState() {
        QuestionRound questionRound = new QuestionRound();
        Question question = new Question();
        question.setQuestionState(submittedState);
        questionRound1Questions.add(question);
        questionRound.setQuestionList(questionRound1Questions);
        QuestionRoundState questionRoundState = questionRoundService.retrieveQuestionRoundState(questionRound);
        assertEquals( submittedState.getState(), questionRoundState.getState());
    }

    @Test
    public void testRetrieveQuestionRoundStateGetsTheLowestLevelState() {
        QuestionRound questionRound = new QuestionRound();
        Question question = new Question();
        question.setQuestionState(draftedState);
        questionRound1Questions.add(question);
        questionRound.setQuestionList(questionRound1Questions);
        QuestionRoundState questionRoundState = questionRoundService.retrieveQuestionRoundState(questionRound);
        assertEquals(draftedState.getState(), questionRoundState.getState());
    }
}
