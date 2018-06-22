package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.coh.Notification.QuestionNotification;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Configuration
public class QuestionServiceTest {

    @InjectMocks
    private QuestionService questionService;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionNotification questionNotification;

    @Mock
    private QuestionStateService questionStateService;

    private Question question;
    private Long questionId = Long.valueOf(2000);

    @Before
    public void setup(){
        QuestionState questionState = new QuestionState();
        questionState.setQuestionStateId(QuestionState.SUBMITTED);

        question = new Question();
        question.setQuestionState(questionState);

        when(questionRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(question));
        when(questionRepository.save(any(Question.class))).thenReturn(new Question());

        when(questionNotification.notifyQuestionState(any(Question.class)))
                .thenReturn(true);

        when(questionStateService.retrieveQuestionStateById(anyInt())).thenReturn(questionState);
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
        QuestionState questionState = new QuestionState();
        questionState.setQuestionStateId(QuestionState.ISSUED);
        boolean success = questionService.updateQuestionState(question, questionState);
        assertEquals(QuestionState.ISSUED, question.getQuestionState().getQuestionStateId());
        assertEquals(true, success);
    }
}
