package uk.gov.hmcts.reform.coh.controller.question;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerRequest;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import static org.junit.Assert.assertEquals;

public class TestQuestionResponse {

    @Test
    public void testMapping() throws Exception {
        String json = JsonUtils.getJsonInput("question/standard_question_response");

        ObjectMapper mapper = new ObjectMapper();
        QuestionResponse request = mapper.readValue(json, QuestionResponse.class);

        assertEquals("How do you do your shopping?", request.getQuestionHeaderText());
        assertEquals("DRAFTED", request.getCurrentState().getName());
        assertEquals("2018-07-06 11:44:42.526", request.getCurrentState().getDatetime());
    }


}
