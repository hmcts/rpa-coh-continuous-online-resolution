package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;

import java.io.IOException;
import java.util.UUID;

@ContextConfiguration
@SpringBootTest
public class QuestionSteps {

    @Value("${base-urls.test-url}")
    private String baseUrl;

    private TestRestTemplate restTemplate = new TestRestTemplate();
    private String ENDPOINT = "/online-hearings";
    private UUID onlineHearingId;
    private HttpHeaders header;
    private Long questionId;

    @Autowired
    private OnlineHearingRepository onlineHearingRepository;

    @Before
    public void setup() {
        header = new HttpHeaders();
        header.add("Content-Type", "application/json");
    }

    @Given("the default online hearing")
    public void createOnlineHearing() throws IOException {
        String externalId = String.valueOf(UUID.randomUUID());
        HttpEntity<String> request = new HttpEntity<>("{\"externalRef\" : \"" + externalId +"\", \"jurisdictionName\" : \"SSCS\"}", header);
        ResponseEntity<OnlineHearing> response = restTemplate.exchange(baseUrl + ENDPOINT, HttpMethod.POST, request, OnlineHearing.class);

        this.onlineHearingId = response.getBody().getOnlineHearingId();
    }

    @And("^the draft a question$")
    public void theDraftAQuestion() throws Throwable {
        HttpEntity<String> request = new HttpEntity<>("{\n" +
                "    \"questionRoundId\": 1,\n" +
                "    \"subject\": \"My Second Question\",\n" +
                "    \"questionText\": \"If a wood chuck could chuck wood?\"\n" +
                "}", header);

        ResponseEntity<Question> response = restTemplate.exchange(baseUrl + ENDPOINT + "/" + onlineHearingId + "/questions", HttpMethod.POST, request, Question.class);
        this.questionId = response.getBody().getQuestionId();
    }

    @When("^set question state to issued$")
    public void setQuestionStateToIssued() throws Throwable {
        HttpEntity<String> request = new HttpEntity<>("", header);

        ResponseEntity<Question> response = restTemplate.exchange(baseUrl + ENDPOINT + "/" + onlineHearingId + "/questions/" + questionId, HttpMethod.GET, request, Question.class);

    }
}
