package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.coh.controller.question.AllQuestionsResponse;
import uk.gov.hmcts.reform.coh.controller.question.CreateQuestionResponse;
import uk.gov.hmcts.reform.coh.controller.question.QuestionRequest;
import uk.gov.hmcts.reform.coh.controller.question.QuestionResponse;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundResponse;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundsResponse;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionStateHistory;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingPanelMemberRepository;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private boolean allQuestionRounds;

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

    @And("^the get request is sent to retrieve all questions$")
    public void get_all_questions_for_a_online_hearing() throws Throwable {
        try {
            OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
            ResponseEntity<String> response = response = restTemplate.getForEntity(baseUrl + ENDPOINT + "/" + onlineHearing.getOnlineHearingId() + "/questions", String.class);
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        } catch (HttpClientErrorException hsee) {
            testContext.getHttpContext().setHttpResponseStatusCode(hsee.getRawStatusCode());
        }
    }

    @Given("^a standard question")
    public void aStandardQuestionRound() throws IOException{
        questionRequest = (QuestionRequest) JsonUtils.toObjectFromTestName("question/standard_question_v_0_0_5", QuestionRequest.class);
        String onlineHearingCaseId = testContext.getScenarioContext().getCurrentOnlineHearing().getCaseId();
        onlineHearing = onlineHearingRepository.findByCaseId(onlineHearingCaseId).get();
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

        allQuestionRounds = true;
    }

    @When("^the get request is sent to get question round ' \"([^\"]*)\" '$")
    public void theGetRequestIsSentToGetQuestionRound(int questionRoundN) {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + ENDPOINT + "/" + onlineHearing.getOnlineHearingId() + "/questionrounds/" + questionRoundN, String.class);
        testContext.getHttpContext().setHttpResponseStatusCode(response.getStatusCodeValue());
        testContext.getHttpContext().setRawResponseString(response.getBody());

        allQuestionRounds = false;
    }

    @When("^the put request is sent to issue the question round ' \"([^\"]*)\" '$")
    public void thePutRequestIsSentToQuestionRound(int questionRoundN) throws Throwable {
        String json = JsonUtils.getJsonInput("question_round/issue_question_round");

        try{
            HttpEntity<String> request = new HttpEntity<>(json, header);
            ResponseEntity<String> response = restTemplate.exchange(baseUrl + ENDPOINT + "/" + onlineHearing.getOnlineHearingId() + "/questionrounds/" + questionRoundN,
                    HttpMethod.PUT, request, String.class);
            testContext.getHttpContext().setRawResponseString(response.getBody());
            testContext.getHttpContext().setHttpResponseStatusCode(response.getStatusCodeValue());
        } catch (HttpClientErrorException hsee) {
            testContext.getHttpContext().setHttpResponseStatusCode(hsee.getRawStatusCode());
        }
    }

    @And("^the question round ' \"([^\"]*)\" ' is ' \"([^\"]*)\" '$")
    public void theQuestionRoundIs(int questionRoundNumber, String expectedState) throws IOException {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundResponse questionRoundResponse;

        if(allQuestionRounds) {
            QuestionRoundsResponse questionRoundsResponse = (QuestionRoundsResponse) JsonUtils.toObjectFromJson(rawJson, QuestionRoundsResponse.class);
            questionRoundResponse = questionRoundsResponse.getQuestionRounds().get(questionRoundNumber - 1);
        }else{
            questionRoundResponse = (QuestionRoundResponse) JsonUtils.toObjectFromJson(rawJson, QuestionRoundResponse.class);

        }
        assertTrue(questionRoundResponse.getQuestionRoundState().getState().equalsIgnoreCase(expectedState));
    }

    @And("^the number of questions rounds is ' \"([^\"]*)\" '$")
    public void theNumberOfQuestionsRoundsIs(int expectedQuestionRounds) throws IOException {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundsResponse questionRoundsResponse = (QuestionRoundsResponse) JsonUtils.toObjectFromJson(rawJson, QuestionRoundsResponse.class);
        int questionRounds = questionRoundsResponse.getQuestionRounds().size();

        assertEquals(expectedQuestionRounds, questionRounds);
    }

    @And("^the previous question round is ' \"([^\"]*)\" '$")
    public void thePreviousQuestionRoundIs(int expectedPreviousQuestionRound) throws Throwable {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundsResponse questionRoundsResponse = (QuestionRoundsResponse) JsonUtils.toObjectFromJson(rawJson, QuestionRoundsResponse.class);
        int previousQuestionRound = questionRoundsResponse.getPreviousQuestionRound();

        assertEquals(expectedPreviousQuestionRound, previousQuestionRound);
    }

    @And("^the current question round is ' \"([^\"]*)\" '$")
    public void theCurrentQuestionRoundIs(int expectedCurrentQuestionRound) throws Throwable {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundsResponse questionRoundsResponse = (QuestionRoundsResponse) JsonUtils.toObjectFromJson(rawJson, QuestionRoundsResponse.class);
        int currentQuestionRound = questionRoundsResponse.getCurrentQuestionRound();

        assertEquals(expectedCurrentQuestionRound, currentQuestionRound);
    }

    @And("^the next question round is ' \"([^\"]*)\" '$")
    public void theNextQuestionRoundIs(int expectedNextQuestionRound) throws Throwable {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundsResponse questionRoundsResponse = (QuestionRoundsResponse) JsonUtils.toObjectFromJson(rawJson, QuestionRoundsResponse.class);
        int nextQuestionRound = questionRoundsResponse.getCurrentQuestionRound();

        assertEquals(expectedNextQuestionRound, nextQuestionRound);
    }

    @And("^the max question round is ' \"([^\"]*)\" '$")
    public void theMaxQuestionRoundIs(int expectedMaxQuestionRound) throws Throwable {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundsResponse questionRoundsResponse = (QuestionRoundsResponse) JsonUtils.toObjectFromJson(rawJson, QuestionRoundsResponse.class);
        int maxQuestionRound = questionRoundsResponse.getCurrentQuestionRound();

        assertEquals(expectedMaxQuestionRound, maxQuestionRound);
    }

    @And("^the number of questions in question round ' \"([^\"]*)\" ' is ' \"([^\"]*)\" '$")
    public void theNumberOfQuestionsInQuestionRoundIs(int questionRoundN, int expectedQuestions) throws Throwable {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundResponse questionRound;

        if (allQuestionRounds) {
            QuestionRoundsResponse questionRoundsResponse = (QuestionRoundsResponse) JsonUtils.toObjectFromJson(rawJson, QuestionRoundsResponse.class);
            questionRound = questionRoundsResponse.getQuestionRounds().get(questionRoundN - 1);
        }else{
            questionRound = (QuestionRoundResponse) JsonUtils.toObjectFromJson(rawJson, QuestionRoundResponse.class);
        }
        assertEquals(expectedQuestions, questionRound.getQuestionList().size());
    }

    @And("^the response contains (\\d) questions$")
    public void the_response_contains_n_questions(int count) throws Throwable {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        ObjectMapper mapper = new ObjectMapper();
        AllQuestionsResponse questionResponses = mapper.readValue(rawJson, AllQuestionsResponse.class);
        assertEquals(count, questionResponses.getQuestions().size());
    }

    @And("^each question in the question round has a history of at least ' \"(\\d)\" ' events$")
    public void eachQuestionInTheQuestionRoundHasHistory(int histories) throws Throwable {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundResponse questionRoundResponse = (QuestionRoundResponse) JsonUtils.toObjectFromJson(rawJson, QuestionRoundResponse.class);
        List<QuestionResponse> questionResponses = questionRoundResponse.getQuestionList();

        List<UUID> questionUUIDs = questionResponses.stream()
                .map(q -> UUID.fromString(q.getQuestionId()))
                .collect(Collectors.toList());

        for (UUID id : questionUUIDs) {
            Optional<Question> optionalQuestion = questionRepository.findById(id);
            if (optionalQuestion.isPresent()){
                List<QuestionStateHistory> questionStateHistories = optionalQuestion.get().getQuestionStateHistories();
                assertTrue(questionStateHistories.size() >= histories);
            }
        }
    }
}
