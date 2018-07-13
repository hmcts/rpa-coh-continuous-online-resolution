package uk.gov.hmcts.reform.coh.controller.question;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.domain.QuestionStateHistory;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class QuestionResponseMapperTest {

    private UUID questionUuid;

    private QuestionState state;

    private Question question;

    @Before
    public void setup() {

        state = new QuestionState();
        state.setState("foo");

        questionUuid = UUID.randomUUID();

        question = new Question();
        question.setQuestionId(questionUuid);
        question.setQuestionRound(1);
        question.setQuestionOrdinal(1);
        question.setQuestionHeaderText("question header");
        question.setQuestionText("question text");
        question.setOwnerReferenceId("bar");
        question.setQuestionState(state);
    }

    @Test
    public void testResponseMappings() {

        Calendar yesterday = new GregorianCalendar();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        List<QuestionStateHistory> histories = new ArrayList<>();
        QuestionStateHistory history1 = new QuestionStateHistory(question, state);
        history1.setDateOccurred(yesterday.getTime());

        Date today = new Date();
        QuestionStateHistory history2 = new QuestionStateHistory(question, state);
        history2.setDateOccurred(today);

        histories.add(history1);
        histories.add(history2);
        question.setQuestionStateHistories(histories);

        QuestionResponse response = new QuestionResponse();
        QuestionResponseMapper.map(question, response);

        // Check each field is mapped correctly
        assertEquals(questionUuid.toString(), response.getQuestionId());
        assertEquals("1", response.getQuestionRound());
        assertEquals("1", response.getQuestionOrdinal());
        assertEquals(question.getQuestionHeaderText(), response.getQuestionHeaderText());
        assertEquals(question.getQuestionText(), response.getQuestionBodyText());
        assertEquals(question.getOwnerReferenceId(), response.getOwnerReference());
        assertEquals(state.getState(), response.getCurrentState().getName());

        // This checks the sorting works
        assertEquals(history2.getDateOccurred().toString(), response.getCurrentState().getDatetime());
    }

    @Test
    public void testMappingsNullDecisionStateHistories() {

        QuestionResponse response = new QuestionResponse();
        QuestionResponseMapper.map(question, response);

        // This checks the sorting works
        assertNull(response.getCurrentState().getDatetime());
    }
}