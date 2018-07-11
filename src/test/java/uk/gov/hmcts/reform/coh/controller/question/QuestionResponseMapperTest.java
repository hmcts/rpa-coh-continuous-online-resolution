package uk.gov.hmcts.reform.coh.controller.question;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.domain.QuestionStateHistory;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class QuestionResponseMapperTest {

    private UUID questionUuid;

    @Before
    public void setup() {
        questionUuid = UUID.randomUUID();
    }

    @Test
    public void testResponseMappings() {
        QuestionState state = new QuestionState();
        state.setState("foo");
        Question question = new Question();
        question.setQuestionId(questionUuid);
        question.setQuestionRound(1);
        question.setQuestionState(state);

        Calendar yesterday = new GregorianCalendar();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        QuestionStateHistory history1 = new QuestionStateHistory(qu
        );


        QuestionResponse response = new QuestionResponse();
        QuestionResponseMapper.map(question, response);

        assertEquals(questionUuid.toString(), response.getQuestionId());
        assertEquals("1", response.getQuestionRound());
    }
}
