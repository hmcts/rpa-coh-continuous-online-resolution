package uk.gov.hmcts.reform.coh.controller.answer;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.AnswerState;
import uk.gov.hmcts.reform.coh.domain.AnswerStateHistory;
import uk.gov.hmcts.reform.coh.states.AnswerStates;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AnswerResponseMapperTest {

    private static final ISO8601DateFormat df = new ISO8601DateFormat();

    private UUID uuid;

    private Answer answer;

    private AnswerState draftedState;

    private AnswerState submittedState;

    private Calendar startDate = new GregorianCalendar();
    private Calendar endDate = new GregorianCalendar();

    @Before
    public void setup() {
        uuid = UUID.randomUUID();

        startDate.set(Calendar.DAY_OF_YEAR, -2);
        endDate.set(Calendar.DAY_OF_YEAR, -1);

        draftedState = new AnswerState();
        draftedState.setState(AnswerStates.DRAFTED.getStateName());
        submittedState = new AnswerState();
        submittedState.setState(AnswerStates.SUBMITTED.getStateName());

        answer = new Answer();
        answer.setAnswerId(uuid);
        answer.setAnswerText("foo");
        answer.setAnswerState(submittedState);

        AnswerStateHistory history1 = new AnswerStateHistory();
        history1.setAnswerstate(draftedState);
        history1.setDateOccured(startDate.getTime());

        AnswerStateHistory history2 = new AnswerStateHistory();
        history2.setAnswerstate(submittedState);
        history2.setDateOccured(endDate.getTime());
        answer.setAnswerStateHistories(Arrays.asList(history1, history2));
    }

    @Test
    public void testMapper() {

        AnswerResponse response = new AnswerResponse();
        AnswerResponseMapper.map(answer, response);

        assertEquals(uuid.toString(), response.getAnswerId());
        assertEquals(answer.getAnswerText(), response.getAnswerText());
        assertEquals(answer.getAnswerState().getState(), response.getStateResponse().getName());
        assertEquals(submittedState.getState(), response.getStateResponse().getName());
        assertEquals(df.format(endDate.getTime()), response.getStateResponse().getDatetime());
    }

    @Test
    public void testMapperWhenNoHistory() {

        answer.setAnswerStateHistories(null);
        AnswerResponse response = new AnswerResponse();
        AnswerResponseMapper.map(answer, response);

        assertEquals(uuid.toString(), response.getAnswerId());
        assertEquals(answer.getAnswerText(), response.getAnswerText());
        assertEquals(answer.getAnswerState().getState(), response.getStateResponse().getName());
        assertEquals(submittedState.getState(), response.getStateResponse().getName());
        assertNull(response.getStateResponse().getDatetime());
    }
}
