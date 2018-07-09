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
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.controller.question.CreateQuestionResponse;
import uk.gov.hmcts.reform.coh.controller.question.QuestionRequest;
import uk.gov.hmcts.reform.coh.controller.question.QuestionResponse;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundResponse;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundsResponse;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingPanelMemberRepository;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@ContextConfiguration
@SpringBootTest
public class QuestionSteps extends BaseSteps{
    private static final Logger log = LoggerFactory.getLogger(QuestionSteps.class);

    private String ENDPOINT = "/continuous-online-hearings";
    private OnlineHearing onlineHearing;
    private HttpHeaders header;
    private Question question;
    private QuestionRequest questionRequest;
    private List<UUID> questionIds;

    @Autowired
    private OnlineHearingRepository onlineHearingRepository;

    @Autowired
    private JurisdictionRepository jurisdictionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private OnlineHearingPanelMemberRepository onlineHearingPanelMemberRepository;

    @Autowired
    public QuestionSteps(TestContext testContext) {
        super(testContext);
    }

    @Before
    public void setup() throws Exception {
        super.setup();
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
            String onlineHearingCaseId = testContext.getScenarioContext().getCurrentOnlineHearing().getCaseId();
            onlineHearingPanelMemberRepository.deleteByOnlineHearing(onlineHearing);
            onlineHearingRepository.deleteByCaseId(onlineHearingCaseId);
        } catch(DataIntegrityViolationException e){
            log.error("Failure may be due to foreign key. This is okay because the online hearing will be deleted elsewhere." + e);
        }

        for(Jurisdiction jurisdiction : testContext.getScenarioContext().getJurisdictions()){
            try {
                jurisdictionRepository.delete(jurisdiction);
            }catch(DataIntegrityViolationException e){
                log.error("Failure may be due to foreign key. This is okay because the online hearing will be deleted elsewhere.");
            }
        }
    }

    @And("^the post request is sent to create the question$")
    public void theDraftAQuestion() throws Throwable {
        String jsonBody = JsonUtils.toJson(questionRequest);
        HttpEntity<String> request = new HttpEntity<>(jsonBody, header);

        int httpResponseCode = 0;
        try{
            ResponseEntity<String> response = restTemplate.exchange(baseUrl + ENDPOINT + "/" + onlineHearing.getOnlineHearingId() + "/questions", HttpMethod.POST, request, String.class);
            String json = response.getBody();
            CreateQuestionResponse createQuestionResponse = (CreateQuestionResponse) JsonUtils.toObjectFromJson(json, CreateQuestionResponse.class);
            questionIds.add(createQuestionResponse.getQuestionId());
            httpResponseCode = response.getStatusCodeValue();
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        } catch (HttpClientErrorException hsee) {
            httpResponseCode = hsee.getRawStatusCode();
        }
        testContext.getHttpContext().setHttpResponseStatusCode(httpResponseCode);
    }

    @Given("^a standard question")
    public void aStandardQuestionRound() throws IOException{
        questionRequest = (QuestionRequest) JsonUtils.toObjectFromTestName("question/standard_question_v_0_0_5", QuestionRequest.class);
        String onlineHearingCaseId = testContext.getScenarioContext().getCurrentOnlineHearing().getCaseId();
        onlineHearing = onlineHearingRepository.findByCaseId(onlineHearingCaseId).get();
    }

    @And("^the question is updated to issued$")
    public void theQuestionIsUpdatedToIssued() throws Throwable {
        QuestionResponse question = (QuestionResponse) JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), QuestionResponse.class);
        String jsonBody = JsonUtils.getJsonInput("question/issue_question");
        HttpEntity<String> request = new HttpEntity<>(jsonBody, header);
        ResponseEntity<Question> response = restTemplate.exchange(
                baseUrl + ENDPOINT + "/" + onlineHearing.getOnlineHearingId() + "/questions/" + question.getQuestionId() + "?_method=patch", HttpMethod.POST, request, Question.class);
        testContext.getHttpContext().setHttpResponseStatusCode(response.getStatusCodeValue());
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

    @When("^the get request is sent to get all question rounds$")
    public void theGetRequestIsSentToGetAllQuestionRounds() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + ENDPOINT + "/" + onlineHearing.getOnlineHearingId() + "/questionrounds", String.class);
        testContext.getHttpContext().setHttpResponseStatusCode(response.getStatusCodeValue());
        testContext.getHttpContext().setRawResponseString(response.getBody());
    }

    @And("^the question round ' \"([^\"]*)\" ' is ' \"([^\"]*)\" '$")
    public void theQuestionRoundIs(int questionRoundNumber, String expectedState) throws IOException {
        String rawJson = testContext.getHttpContext().getRawResponseString();

        QuestionRoundsResponse questionRoundsResponse = (QuestionRoundsResponse) JsonUtils.toObjectFromJson(rawJson, QuestionRoundsResponse.class);
        QuestionRoundResponse questionRoundResponse = questionRoundsResponse.getQuestionRounds().get(questionRoundNumber -1);

        assertTrue(questionRoundResponse.getQuestionRoundState().getState().equalsIgnoreCase(expectedState));
    }

    @And("^the number of questions rounds is ' \"([^\"]*)\" '$")
    public void theNumberOfQuestionsRoundsIs(int expectedQuestionRounds) throws IOException {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundsResponse questionRoundsResponse = (QuestionRoundsResponse) JsonUtils.toObjectFromJson(rawJson, QuestionRoundsResponse.class);
        int questionRounds = questionRoundsResponse.getQuestionRounds().size();

        assertEquals(expectedQuestionRounds, questionRounds);
    }
}
