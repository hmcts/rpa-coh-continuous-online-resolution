package uk.gov.hmcts.reform.coh.service;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.controller.exceptions.NotAValidUpdateException;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;
import uk.gov.hmcts.reform.coh.service.utils.QuestionDeadlineUtils;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import javax.persistence.EntityNotFoundException;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.coh.states.QuestionStates.DRAFTED;

@RunWith(SpringRunner.class)
public class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionStateService questionStateService;

    @Mock
    private QuestionRoundService questionRoundService;

    @Mock
    private QuestionDeadlineUtils utils;

    private QuestionService questionService;

    private QuestionState drafted = new QuestionState("question_drafted");
    private QuestionState issued = new QuestionState("question_issued");
    private Question question;

    private OnlineHearing onlineHearing;
    private static UUID ONE;
    private QuestionState issuedState;
    private QuestionState grantedState;
    private QuestionState deniedState;

    @Before
    public void setup() {
        ONE = UUID.randomUUID();
        onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(ONE);

        issuedState = mockQuestionState(QuestionStates.ISSUED);
        when(questionStateService.fetchQuestionState(QuestionStates.ISSUED)).thenReturn(issuedState);

        grantedState = mockQuestionState(QuestionStates.QUESTION_DEADLINE_EXTENSION_GRANTED);
        when(questionStateService.fetchQuestionState(QuestionStates.QUESTION_DEADLINE_EXTENSION_GRANTED)).thenReturn(grantedState);

        deniedState = mockQuestionState(QuestionStates.QUESTION_DEADLINE_EXTENSION_GRANTED);
        when(questionStateService.fetchQuestionState(QuestionStates.QUESTION_DEADLINE_EXTENSION_DENIED)).thenReturn(deniedState);

        questionService = new QuestionService(questionRepository, questionStateService, questionRoundService, utils);
        given(questionRoundService.isQrValidTransition(any(Question.class), any(OnlineHearing.class))).willReturn(true);
        given(questionRoundService.isQrValidState(any(Question.class), any(OnlineHearing.class))).willReturn(true);

        given(questionStateService.retrieveQuestionStateByStateName(DRAFTED.getStateName())).willReturn(Optional.ofNullable(drafted));
        given(questionStateService.retrieveQuestionStateByStateName(QuestionStates.ISSUED.getStateName())).willReturn(Optional.ofNullable(issued));
        question = new Question();
        question.setQuestionState(drafted);
        when(questionRepository.save(question)).thenReturn(question);

        when(utils.isEligibleForDeadlineExtension(any())).thenReturn(true);
    }

    @Test
    public void testUpdateADraftQuestion() {
        questionService.updateQuestion(question);
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDraftQuestionNotFound() {
        given(questionStateService.retrieveQuestionStateByStateName(DRAFTED.getStateName())).willReturn(Optional.empty());
        questionService.updateQuestion(question);
    }

    @Test(expected = NotAValidUpdateException.class)
    public void testUpdateQuestionThrowsNotAValidUpdateIfNotDraftState() {
        question.setQuestionState(issued);
        questionService.updateQuestion(question);
    }

    @Test
    public void testCreateQuestion() {
        when(questionRepository.save(question)).thenReturn(question);
        when(questionStateService.retrieveQuestionStateByStateName(DRAFTED.getStateName())).thenReturn(Optional.ofNullable(drafted));

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(ONE);
        Question newQuestion = questionService.createQuestion(question, onlineHearing);
        verify(questionStateService, times(1)).retrieveQuestionStateByStateName(DRAFTED.getStateName());
        assertEquals(newQuestion, question);
    }

    @Test(expected = NotAValidUpdateException.class)
    public void testCreateQuestionWithInvalidUpdate() {
        when(questionRepository.save(question)).thenReturn(question);
        when(questionStateService.retrieveQuestionStateById(1)).thenReturn(drafted);
        given(questionRoundService.isQrValidTransition(any(Question.class), any(OnlineHearing.class))).willReturn(false);

        questionService.createQuestion(question, onlineHearing);
    }

    @Test(expected = NotAValidUpdateException.class)
    public void testCreateQuestionForIssuedQuestionRoundThrowsException() {
        when(questionRoundService.isQrValidState(any(Question.class), any(OnlineHearing.class))).thenReturn(false);
        questionService.createQuestion(question, onlineHearing);
    }

    @Test
    public void testRetrieveQuestion() {
        when(questionRepository.findById(ONE)).thenReturn(Optional.of(question));
        when(questionStateService.retrieveQuestionStateById(1)).thenReturn(drafted);

        Optional<Question> newQuestion = questionService.retrieveQuestionById(ONE);
        verify(questionRepository, times(1)).findById(ONE);
        assertEquals(question, newQuestion.get());
    }
    @Test
    public void testDelete() {
        doNothing().when(questionRepository).delete(question);
        questionService.deleteQuestion(question);
        verify(questionRepository, times(1)).delete(question);
    }

    @Test
    public void testFindAllQuestionsByOnlineHearing() {
        List<Question> questions = new ArrayList<>();
        questions.add(question);

        given(questionRepository.findAllByOnlineHearing(onlineHearing)).willReturn(questions);
        Optional<List<Question>> responses = questionService.findAllQuestionsByOnlineHearing(onlineHearing);

        assertTrue(responses.isPresent());
        assertEquals(1, responses.get().size());
    }

    @Test
    public void testFindAllQuestionsByOnlineHearingNone() {
        given(questionRepository.findAllByOnlineHearing(onlineHearing)).willReturn(null);
        Optional<List<Question>> responses = questionService.findAllQuestionsByOnlineHearing(onlineHearing);

        assertFalse(responses.isPresent());
    }

    @Test
    public void testUpdateQuestionForced() {
        questionService.updateQuestionForced(question);
        verify(questionRepository, times(1)).save(question);
    }

    @Test
    public void testRequestingDeadlineExtensionForExpiredQuestion() {
        when(utils.isEligibleForDeadlineExtension(any())).thenReturn(false);
        Question mockedQuestion = expiredQuestion();
        when(questionRepository.findAllByOnlineHearing(onlineHearing)).thenReturn(
            ImmutableList.of(mockedQuestion)
        );

        questionService.requestDeadlineExtension(onlineHearing);

        verify(mockedQuestion, times(0)).setDeadlineExpiryDate(any());
    }

    @Test
    public void testRequestingDeadlineExtensionForIssuedQuestion() throws Throwable {
        Question mockedQuestion = notExpiredQuestion();
        doReturn(issuedState).when(mockedQuestion).getQuestionState();

        when(questionRepository.findAllByOnlineHearing(onlineHearing)).thenReturn(
            ImmutableList.of(mockedQuestion)
        );

        questionService.requestDeadlineExtension(onlineHearing);

        verify(mockedQuestion, times(1)).setDeadlineExpiryDate(any());
        verify(mockedQuestion, times(1)).setQuestionState(grantedState);
        verify(mockedQuestion, times(1)).updateQuestionStateHistory(grantedState);
    }

    @Test
    public void testRequestingDeadlineExtensionForGrantedQuestion() throws Throwable {
        Question mockedQuestion = notExpiredQuestion();
        doReturn(grantedState).when(mockedQuestion).getQuestionState();

        when(questionRepository.findAllByOnlineHearing(onlineHearing)).thenReturn(
            ImmutableList.of(mockedQuestion)
        );

        questionService.requestDeadlineExtension(onlineHearing);

        verify(mockedQuestion, times(0)).setDeadlineExpiryDate(any());
        verify(mockedQuestion, times(1)).setQuestionState(deniedState);
        verify(mockedQuestion, times(1)).updateQuestionStateHistory(deniedState);
    }

    @Test
    public void testRequestingDeadlineExtensionForPendingQuestion() throws Throwable {
        Question mockedQuestion = notExpiredQuestion();
        QuestionState pendingState = mockQuestionState(QuestionStates.ISSUE_PENDING);
        doReturn(pendingState).when(mockedQuestion).getQuestionState();

        when(questionRepository.findAllByOnlineHearing(onlineHearing)).thenReturn(
            ImmutableList.of(mockedQuestion)
        );

        questionService.requestDeadlineExtension(onlineHearing);

        verify(mockedQuestion, times(0)).setDeadlineExpiryDate(any());
        verify(mockedQuestion, times(0)).setQuestionState(any());
        verify(mockedQuestion, times(0)).updateQuestionStateHistory(any());
    }

    private QuestionState mockQuestionState(QuestionStates state) {
        QuestionState spy = spy(QuestionState.class);
        doReturn(state.getStateName()).when(spy).getState();
        return spy;
    }

    private Question expiredQuestion() {
        Question mockedQuestion = spy(Question.class);
        doReturn(Date.from(Instant.now().minus(1, ChronoUnit.DAYS))).when(mockedQuestion).getDeadlineExpiryDate();
        return mockedQuestion;
    }

    private Question notExpiredQuestion() {
        Question mockedQuestion = spy(Question.class);
        doReturn(Date.from(Instant.now().plus(1, ChronoUnit.DAYS))).when(mockedQuestion).getDeadlineExpiryDate();
        return mockedQuestion;
    }
}
