package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.controller.exceptions.NotAValidUpdateException;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionStateService questionStateService;

    @Mock
    private QuestionRoundService questionRoundService;

    private QuestionService questionService;

    private QuestionState drafted = new QuestionState("question_drafted");
    private QuestionState issued = new QuestionState("question_issued");
    private Question question;

    private OnlineHearing onlineHearing;
    private static UUID ONE;

    @Before
    public void setup() {
        ONE = UUID.randomUUID();
        onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(ONE);

        questionService = new QuestionService(questionRepository, questionStateService, questionRoundService);
        given(questionRoundService.isQrValidTransition(any(Question.class), any(OnlineHearing.class))).willReturn(true);
        given(questionRoundService.isQrValidState(any(Question.class), any(OnlineHearing.class))).willReturn(true);

        given(questionStateService.retrieveQuestionStateByStateName(QuestionStates.DRAFTED.getStateName())).willReturn(Optional.ofNullable(drafted));
        given(questionStateService.retrieveQuestionStateByStateName(QuestionStates.ISSUED.getStateName())).willReturn(Optional.ofNullable(issued));
        question = new Question();
        question.setQuestionState(drafted);
        when(questionRepository.save(question)).thenReturn(question);
    }

    @Test
    public void testUpdateADraftQuestion() {
        questionService.updateQuestion(question);
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @Test(expected = NotAValidUpdateException.class)
    public void testUpdateQuestionThrowsNotAValidUpdateIfNotDraftState() {
        question.setQuestionState(issued);
        questionService.updateQuestion(question);
    }

    @Test
    public void testCreateQuestion() {
        when(questionRepository.save(question)).thenReturn(question);
        when(questionStateService.retrieveQuestionStateByStateName(QuestionStates.DRAFTED.getStateName())).thenReturn(Optional.ofNullable(drafted));

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(ONE);
        Question newQuestion = questionService.createQuestion(question, onlineHearing);
        verify(questionStateService, times(1)).retrieveQuestionStateByStateName(QuestionStates.DRAFTED.getStateName());
        assertEquals(newQuestion, question);
    }

    @Test(expected = NotAValidUpdateException.class)
    public void testCreateQuestionWithInvalidUpdate() {
        when(questionRepository.save(question)).thenReturn(question);
        when(questionStateService.retrieveQuestionStateById(1)).thenReturn(drafted);
        given(questionRoundService.isQrValidTransition(any(Question.class), any(OnlineHearing.class))).willReturn(false);

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
}
