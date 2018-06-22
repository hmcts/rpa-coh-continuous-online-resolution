package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.Notification.QuestionNotification;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import java.util.Optional;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionStateService questionStateService;

    private QuestionState drafted = new QuestionState("DRAFTED");
    private QuestionState issued = new QuestionState("ISSUED");
    private Question question;

    private static final Long ONE = 1L;

    @Mock
    private QuestionNotification questionNotification;

    @Mock
    private OnlineHearingService onlineHearingService;

    private Long questionId = Long.valueOf(2000);

    @InjectMocks
    private QuestionService questionService;

    @Before
    public void setup(){
        question = new Question();
        question.setQuestionState(issued);

        when(questionRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(question));
        when(questionRepository.save(any(Question.class))).thenReturn(new Question());

        when(questionNotification.notifyQuestionState(any(Question.class)))
                .thenReturn(true);

        when(questionStateService.retrieveQuestionStateById(anyInt())).thenReturn(drafted);
        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).thenReturn(Optional.of(new OnlineHearing()));
    }

    @Test
    public void testCreateQuestion() {
        when(questionRepository.save(question)).thenReturn(question);
        when(questionStateService.retrieveQuestionStateById(1)).thenReturn(drafted);

        /**
         * This needs to be fixed so that online hearing id is an attribute of question
         */
        Question newQuestion = questionService.createQuestion(question, UUID.randomUUID());
        verify(questionStateService, times(1)).retrieveQuestionStateById(1);
        assertEquals(newQuestion, question);
    }

    @Test
    public void testRetrieveQuestion() {
        when(questionRepository.findById(ONE)).thenReturn(Optional.of(question));
        when(questionStateService.retrieveQuestionStateById(1)).thenReturn(drafted);

        Question newQuestion = questionService.retrieveQuestionById(ONE);
        verify(questionRepository, times(1)).findById(ONE);
        assertEquals(newQuestion, question);
    }

    @Test
    public void testEditQuestion() {
        when(questionRepository.save(question)).thenReturn(question);
        when(questionRepository.findById(ONE)).thenReturn(Optional.of(question));
        when(questionStateService.retrieveQuestionStateById(2)).thenReturn(issued);

        /**
         * This needs to be fixed so that question id is an attribute of question
         */
        Question newQuestion = questionService.editQuestion(ONE, question);
        verify(questionRepository, times(1)).findById(ONE);
        assertEquals("Correct state", issued, newQuestion.getQuestionState());
        assertEquals("Event logged", 1, newQuestion.getQuestionStateHistories().size());
    }


    @Test
    public void testGetQuestionRoundReturnsQuestionRoundObject(){
        Question returnedQuestion = questionService.retrieveQuestionById(questionId);
        assertTrue(returnedQuestion.equals(question));
        verify(questionRepository).findById(any(Long.class));
    }

    @Test
    public void testRequestToSSCSEndpointWhenDownReturnsFalse(){
        when(questionNotification.notifyQuestionState(any(Question.class)))
                .thenReturn(false);
        boolean success = questionService.issueQuestion(question);
        assertEquals(false, success);
    }
    @Test
    public void testQuestionStateIsSavedToDatabaseIfNotifyicationReturnsTrue(){
        questionService.issueQuestion(question);
        verify(questionRepository).save(any(Question.class));
    }
    @Test
    public void testUpdateQuestionRoundToIssued(){
        boolean success = questionService.updateQuestionState(question, issued);
        assertEquals(true, success);
    }
}