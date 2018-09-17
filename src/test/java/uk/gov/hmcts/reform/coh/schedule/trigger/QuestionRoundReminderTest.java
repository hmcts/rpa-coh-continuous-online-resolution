package uk.gov.hmcts.reform.coh.schedule.trigger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.service.QuestionStateService;
import uk.gov.hmcts.reform.coh.util.QuestionStateUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.coh.states.QuestionStates.ISSUED;
import static uk.gov.hmcts.reform.coh.states.QuestionStates.QUESTION_DEADLINE_EXTENSION_GRANTED;

@RunWith(SpringRunner.class)
public class QuestionRoundReminderTest {

    @Mock
    private QuestionService questionService;

    @Mock
    private QuestionStateService stateService;

    @InjectMocks
    private QuestionRoundReminder trigger;

    private Date start;
    private Date end;

    @Captor
    private ArgumentCaptor<List<QuestionState>> statesParam;

    @Before
    public void setUp() {
        start = java.sql.Timestamp.valueOf(LocalDate.now().plusDays(1).atStartOfDay());
        end = java.sql.Timestamp.valueOf(LocalDate.now().plusDays(1).atStartOfDay().plusHours(1));
        when(stateService.retrieveQuestionStateByStateName(ISSUED.getStateName())).thenReturn(Optional.ofNullable(QuestionStateUtils.get(ISSUED)));
        when(stateService.retrieveQuestionStateByStateName(QUESTION_DEADLINE_EXTENSION_GRANTED.getStateName())).thenReturn(Optional.ofNullable(QuestionStateUtils.get(QUESTION_DEADLINE_EXTENSION_GRANTED)));
        when(questionService.retrieveQuestionsDeadlineExpiredBetweenAndQuestionStates(any(Date.class), any(Date.class), anyList())).thenReturn(Collections.emptyList());
    }

    @Test
    public void testFilterCalledWithCorrectParameters() {
        ArgumentCaptor<Date> startParam = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Date> endParam = ArgumentCaptor.forClass(Date.class);
        trigger.getQuestions();
        Mockito.verify(questionService, times(1))
                .retrieveQuestionsDeadlineExpiredBetweenAndQuestionStates(startParam.capture(), endParam.capture(), statesParam.capture());
        assertEquals(start, startParam.getValue());
        assertEquals(end, endParam.getValue());
        assertEquals(2, statesParam.getValue().size());
        assertEquals(1, statesParam.getValue().stream().filter(s -> s.getState().equalsIgnoreCase(ISSUED.getStateName())).count());
        assertEquals(1, statesParam.getValue().stream().filter(s -> s.getState().equalsIgnoreCase(QUESTION_DEADLINE_EXTENSION_GRANTED.getStateName())).count());
    }

    @Test
    public void testGetEventType() {
        assertEquals(EventTypes.QUESTION_DEADLINE_REMINDER, trigger.getEventType());
    }
}