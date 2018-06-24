package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.AnswerState;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.repository.AnswerStateRepository;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class AnswerStateServiceTest {

    @Mock
    private AnswerStateRepository answerStateRepository;

    private AnswerStateService answerStateService;

    private AnswerState answerState;

    private static final Long ONE = 1L;

    @Before
    public void setup() {
        answerState = new AnswerState();
        answerState.setState("foo");
        answerStateService = new AnswerStateService(answerStateRepository);
    }

    @Test
    public void testRetrieveAnswerState() {
        when(answerStateRepository.findByState("foo")).thenReturn(Optional.of(answerState));

        AnswerState newAnswerState = answerStateService.retrieveAnswerStateByState("foo").get();
        assertEquals(answerState, newAnswerState);
    }

    @Test
    public void testRetrieveAnswerStateFail() {
        when(answerStateRepository.findByState("foo")).thenReturn(Optional.of(answerState));

        Optional<AnswerState> newAnswerState = answerStateService.retrieveAnswerStateByState("bar");
        assertFalse(newAnswerState.isPresent());
    }
}
