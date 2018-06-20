package uk.gov.hmcts.reform.coh.controller.answer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import static org.junit.Assert.assertEquals;

public class TestAnswerRequest {

    @Test
    public void testMapping() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");

        ObjectMapper mapper = new ObjectMapper();
        AnswerRequest request = mapper.readValue(json, AnswerRequest.class);

        assertEquals("123", request.getQuestionId());
        assertEquals("456", request.getQuestionPartId());
        assertEquals("freetext", request.getAnswer().getTypeOfAnswer());
        assertEquals("string", request.getAnswer().getAnswer());
        assertEquals("answer_submitted", request.getAnswerState().getStateName());
        assertEquals("2018-06-16T19:20:30.45+01:00", request.getAnswerState().getStateStartTime());
    }
}
