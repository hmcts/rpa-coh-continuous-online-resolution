package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;
import java.util.Map;

@ContextConfiguration
@SpringBootTest
public class QuestionSteps {

    @Value("${base-urls.test-url}")
    private String baseUrl;

    private Map<String, String> endpoints = new HashMap<String, String>();

    @Before
    public void setup() {
        endpoints.put("answer", "/online-hearings/1/questions/question_id/answers");
        endpoints.put("question", "/online-hearings/1/questions");
       // questionId = null;
        //answerId = null;
    }


}
