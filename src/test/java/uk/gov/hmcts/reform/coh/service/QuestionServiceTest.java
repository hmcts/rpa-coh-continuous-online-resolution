package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionStateService questionStateService;

    private QuestionService questionService;

    private QuestionState drafted = new QuestionState("DRAFTED");
    private QuestionState issued = new QuestionState("ISSUED");
    private Question question;

    private static final Long ONE = 1L;

    @Before
    public void setup() {
        question = new Question();
        questionService = new QuestionService(questionRepository, questionStateService);
    }

    @Test
    public void testCreateQuestion() {
        when(questionRepository.save(question)).thenReturn(question);
        when(questionStateService.retrieveQuestionStateById(1)).thenReturn(drafted);

        /**
         * This needs to be fixed so that online hearing id is an attribute of question
         */
        Question newQuestion = questionService.createQuestion(1, question);
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
}
