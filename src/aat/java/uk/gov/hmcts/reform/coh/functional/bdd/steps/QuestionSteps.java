package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.controller.question.CreateQuestionResponse;
import uk.gov.hmcts.reform.coh.controller.question.QuestionRequest;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestTrustManager;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingPanelMemberRepository;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;

@ContextConfiguration
@SpringBootTest
public class QuestionSteps extends BaseSteps{
    private static final Logger log = LoggerFactory.getLogger(QuestionSteps.class);

    private RestTemplate restTemplate;
    private String ENDPOINT = "/online-hearings";
    private OnlineHearing onlineHearing;
    private HttpHeaders header;
    private Question question;
    private QuestionRequest questionRequest;
    private List<UUID> questionIds;

    @Autowired
    private OnlineHearingRepository onlineHearingRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private OnlineHearingPanelMemberRepository onlineHearingPanelMemberRepository;

    private TestContext testContext;

    @Autowired
    public QuestionSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    @Before
    public void setup() throws Exception {
        restTemplate = new RestTemplate(TestTrustManager.getInstance().getTestRequestFactory());
        header = new HttpHeaders();
        header.add("Content-Type", "application/json");
        questionIds = new ArrayList<>();
    }

    @After
    public void cleanUp() {
        for (UUID questionId : questionIds) {
            questionRepository.deleteById(questionId);
        }

        try {
            String onlineHearingExternalRef = testContext.getScenarioContext().getCurrentOnlineHearing().getExternalRef();
            onlineHearingPanelMemberRepository.deleteByOnlineHearing(onlineHearing);
            onlineHearingRepository.deleteByExternalRef(onlineHearingExternalRef);
        } catch(DataIntegrityViolationException e){
            log.error("Failure may be due to foreign key. This is okay because the online hearing will be deleted elsewhere." + e);
        }
    }

    @And("^the post request is sent to create the question and the response status is (\\d+)$")
    public void theDraftAQuestion(int responseCode) throws Throwable {
        String jsonBody = JsonUtils.toJson(questionRequest);
        HttpEntity<String> request = new HttpEntity<>(jsonBody, header);

        int httpResponseCode = 0;
        try{
            ResponseEntity<String> response = restTemplate.exchange(baseUrl + ENDPOINT + "/" + onlineHearing.getOnlineHearingId() + "/questions", HttpMethod.POST, request, String.class);
            String json = response.getBody();
            CreateQuestionResponse createQuestionResponse = (CreateQuestionResponse) JsonUtils.toObjectFromJson(json, CreateQuestionResponse.class);
            questionIds.add(createQuestionResponse.getQuestionId());
            httpResponseCode = response.getStatusCodeValue();
        } catch (HttpClientErrorException hsee) {
            httpResponseCode = hsee.getRawStatusCode();
        }

        assertEquals(responseCode, httpResponseCode);
    }

    @Given("^a standard question")
    public void aStandardQuestionRound() throws IOException{
        questionRequest = (QuestionRequest) JsonUtils.toObjectFromTestName("question/standard_question_v_0_0_5", QuestionRequest.class);
        String onlineHearingExternalRef = testContext.getScenarioContext().getCurrentOnlineHearing().getExternalRef();
        onlineHearing = onlineHearingRepository.findByExternalRef(onlineHearingExternalRef).get();
    }

    @Given("^the question round is ' \"([^\"]*)\" '$")
    public void theQuestionRoundIs1(String questionRound){
        questionRequest.setQuestionRound(questionRound);
    }

    @Then("^the question state is ' \"([^\"]*)\" '$")
    public void theQuestionStateIs(String expectedState) {
       String state = question.getQuestionState().getState();
       assertEquals(expectedState, state);
    }

    @When("^a patch request is sent to ' \"([^\"]*)\" ' and response status is ' \"([^\"]*)\" '$")
    public void aPatchRequestIsSentToOnlineHearingsOnlineHearingIdQuestionsQuestionId(String endpoint, String expectedStatus) throws Throwable {

        endpoint = endpoint.replaceAll("onlineHearing_id", String.valueOf(onlineHearing.getOnlineHearingId()));
        endpoint = endpoint.replaceAll("question_id", String.valueOf(question.getQuestionId()));

        /**
         * This is a workaround for https://jira.spring.io/browse/SPR-15347
         *
         **/
        String jsonBody = JsonUtils.getJsonInput("question/issue_question");
        HttpEntity<String> request = new HttpEntity<>(jsonBody, header);
        int httpResponseCode = 0;
        try {
            ResponseEntity<Question> response = restTemplate.exchange(baseUrl + endpoint + "?_method=patch", HttpMethod.POST, request, Question.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                question = response.getBody();
            }
            httpResponseCode = response.getStatusCode().value();
        } catch (HttpServerErrorException hsee) {
            httpResponseCode = hsee.getRawStatusCode();
        }

        if(expectedStatus.contains("Successful")){
            assertEquals(200, httpResponseCode);
        }else if(expectedStatus.contains("Server error")){
            assertEquals(500, httpResponseCode);
        }
    }
}
