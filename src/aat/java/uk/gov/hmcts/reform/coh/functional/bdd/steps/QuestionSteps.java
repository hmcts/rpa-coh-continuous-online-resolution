package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ContextConfiguration
@SpringBootTest
public class QuestionSteps {

    @Value("${base-urls.test-url}")
    private String baseUrl;

    private Map<String, String> endpoints = new HashMap<String, String>();
    private TestRestTemplate restTemplate = new TestRestTemplate();
    private String ENDPOINT = "/online-hearings";
    private ResponseEntity<String> response;

    @Autowired
    private OnlineHearingRepository onlineHearingRepository;

    @Before
    public void setup() {
        endpoints.put("answer", "/online-hearings/1/questions/question_id/answers");
        endpoints.put("question", "/online-hearings/1/questions");
       // questionId = null;
        //answerId = null;
    }


    @Given("the default online hearing")
    public void createOnlineHearing() throws IOException {
        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");

        String externalId = String.valueOf(UUID.randomUUID());
        HttpEntity<String> request = new HttpEntity<>("{\"externalRef\" : \"" + externalId +"\", \"jurisdictionName\" : \"SSCS\"}", header);
        response = restTemplate.exchange(baseUrl + ENDPOINT, HttpMethod.POST, request, String.class);
    }

    @Then("^remove test data ' \"([^\"]*)\" '$")
    public void removeTestData(String externalRef) throws Throwable {
        //onlineHearingRepository.delete(onlineHearing);
    }

}
