package uk.gov.hmcts.reform.coh.controller.question;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerRequest;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class TestQuestionResponse {

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
        QuestionResponse response = new QuestionResponse();
        QuestionResponseMapper.map(question, response);

        assertEquals(questionUuid.toString(), response.getQuestionId());
        assertEquals("1", response.getQuestionRound());
    }
}
