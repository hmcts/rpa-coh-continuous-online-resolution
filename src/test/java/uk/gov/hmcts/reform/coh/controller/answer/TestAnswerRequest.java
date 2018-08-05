package uk.gov.hmcts.reform.coh.controller.answer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import static org.junit.Assert.assertEquals;

public class TestAnswerRequest {

    @Test
    public void testMapping() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");

        ObjectMapper mapper = new ObjectMapper();
        AnswerRequest request = mapper.readValue(json, AnswerRequest.class);

        assertEquals("answer_drafted", request.getAnswerState());
        assertEquals("string", request.getAnswerText());
    }
}
