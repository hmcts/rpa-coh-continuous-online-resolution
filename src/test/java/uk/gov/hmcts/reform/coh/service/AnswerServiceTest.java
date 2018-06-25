package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.repository.AnswerRepository;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class AnswerServiceTest {

    @Mock
    private AnswerRepository answerRepository;

    private AnswerService answerService;

    private Answer answer;

    private static final Long ONE = 1L;

    @Before
    public void setup() {
        answer = new Answer();
        answerService = new AnswerService(answerRepository);
    }

    @Test
    public void testCreateAnswer() {
        when(answerRepository.save(answer)).thenReturn(answer);

        Answer newAnswer = answerService.createAnswer(answer);
        assertEquals(newAnswer, answer);
    }

    @Test
    public void testRetrieveAnswerById() {
        when(answerRepository.findById(ONE)).thenReturn(Optional.of(answer));
        Optional<Answer> newAnswer = answerService.retrieveAnswerById(ONE);
        assertTrue(newAnswer.isPresent());
    }

    @Test
    public void testRetrieveAnswerByIdFail() {
        when(answerRepository.findById(ONE)).thenReturn(Optional.empty());
        Optional<Answer> newAnswer = answerService.retrieveAnswerById(ONE);
        assertFalse(newAnswer.isPresent());
    }

    @Test
    public void testFindByQuestion() {
        Question question = new Question();
        List<Answer> answerList = new ArrayList<>();
        answerList.add(answer);
        when(answerRepository.findByQuestion(question)).thenReturn(answerList);
        List<Answer> answers = answerService.retrieveAnswersByQuestion(question);
        assertFalse("Not empty", answers.isEmpty());
        assertEquals("Answers count", 1, answers.size());
    }

    @Test
    public void testUpdateAnswerById() {
        when(answerRepository.existsById(ONE)).thenReturn(true);
        when(answerRepository.save(answer)).thenReturn(answer);
        answer.setAnswerId(ONE);
        Answer newAnswer = answerService.updateAnswerById(answer);
        assertEquals(newAnswer, answer);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testUpdateAnswerByIdFail() {
        when(answerRepository.existsById(ONE)).thenReturn(true);
        when(answerRepository.save(answer)).thenReturn(answer);
        answerService.updateAnswerById(answer);
    }

    @Test
    public void tesDelete() {
        doNothing().when(answerRepository).delete(answer);
        answerService.deleteAnswer(answer);
        verify(answerRepository, times(1)).delete(answer);
    }
}
