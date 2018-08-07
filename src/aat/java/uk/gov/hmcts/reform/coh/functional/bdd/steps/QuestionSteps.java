package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.PendingException;
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
import uk.gov.hmcts.reform.coh.controller.question.*;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundResponse;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundsResponse;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingPanelMemberRepository;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
            try {
                questionRepository.deleteById(questionId);
            } catch (Exception e) {
                // Don't care
            }

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
        onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        int httpResponseCode = 0;
        try{
            ResponseEntity<String> response = restTemplate.exchange(baseUrl + ENDPOINT + "/" + onlineHearing.getOnlineHearingId() + "/questions", HttpMethod.POST, request, String.class);
            String json = response.getBody();
            CreateQuestionResponse createQuestionResponse = (CreateQuestionResponse) JsonUtils.toObjectFromJson(json, CreateQuestionResponse.class);
            questionIds.add(createQuestionResponse.getQuestionId());
            httpResponseCode = response.getStatusCodeValue();
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
            testContext.getScenarioContext().setCurrentQuestion(extractQuestion(createQuestionResponse));
        } catch (HttpClientErrorException hsee) {
            httpResponseCode = hsee.getRawStatusCode();
        }
        testContext.getHttpContext().setHttpResponseStatusCode(httpResponseCode);
    }

    @And("^the get request is sent to retrieve all questions$")
    public void get_all_questions_for_a_online_hearing() throws Throwable {
        try {
            OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
            ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + ENDPOINT + "/" + onlineHearing.getOnlineHearingId() + "/questions", String.class);
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        } catch (HttpClientErrorException hsee) {
            testContext.getHttpContext().setHttpResponseStatusCode(hsee.getRawStatusCode());
        }
    }

    @And("^the get request is sent to retrieve the submitted question$")
    public void get_the_submitted_question() throws Throwable {
        try {
            OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
            Question question = testContext.getScenarioContext().getCurrentQuestion();
            ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + ENDPOINT + "/" + onlineHearing.getOnlineHearingId() + "/questions/" + question.getQuestionId(), String.class);
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        } catch (HttpClientErrorException hsee) {
            testContext.getHttpContext().setHttpResponseStatusCode(hsee.getRawStatusCode());
        }
    }

    @Given("^a standard question")
    public void aStandardQuestionRound() throws IOException{
        questionRequest = (QuestionRequest) JsonUtils.toObjectFromTestName("question/standard_question_v_0_0_5", QuestionRequest.class);
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
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + ENDPOINT + "/" + onlineHearing.getOnlineHearingId() + "/questionrounds/" + questionRoundN, String.class);
        testContext.getHttpContext().setHttpResponseStatusCode(response.getStatusCodeValue());
        testContext.getHttpContext().setRawResponseString(response.getBody());

        allQuestionRounds = false;
    }

    @When("^the put request is sent to issue the question round ' \"([^\"]*)\" '$")
    public void thePutRequestIsSentToQuestionRound(int questionRoundN) throws Throwable {
        String json = JsonUtils.getJsonInput("question_round/issue_question_round");

        try {
            OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
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

    @And("^the question id matches$")
    public void the_question_id_matches() throws Throwable {
        QuestionResponse question = (QuestionResponse) JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), QuestionResponse.class);
        assertEquals(testContext.getScenarioContext().getCurrentQuestion().getQuestionId().toString(), question.getQuestionId());
    }

    @And("^the question state name is (.*)$")
    public void the_question_state_name_is(String stateName) throws Throwable {
        QuestionResponse question = (QuestionResponse) JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), QuestionResponse.class);
        assertEquals(stateName, question.getCurrentState().getName());
    }

    @And("^the question state timestamp is today$")
    public void the_question_state_timestamp_is_today() throws Throwable {
        QuestionResponse question = (QuestionResponse) JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), QuestionResponse.class);
        assertTrue(question.getCurrentState().getDatetime().contains(df.format(new Date())));
    }

    private Question extractQuestion(CreateQuestionResponse response) {
        Question question = new Question();
        question.setQuestionId(response.getQuestionId());

        return question;
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

    @When("^the question body is edited to ' \"([^\"]*)\" '$")
    public void theQuestionBodyIsEditedToSomeNewText(String questionBody) throws IOException {
        String json = JsonUtils.getJsonInput("question/update_question");

        UpdateQuestionRequest updateQuestionRequest = (UpdateQuestionRequest) JsonUtils.toObjectFromJson(json, UpdateQuestionRequest.class);
        updateQuestionRequest.setQuestionBodyText(questionBody);
        testContext.getScenarioContext().setUpdateQuestionRequest(updateQuestionRequest);
    }

    @Given("^the question header is edited to ' \"([^\"]*)\" '$")
    public void theQuestionHeaderIsEditedTo(String questionHeader) throws Throwable {
        String json = JsonUtils.getJsonInput("question/update_question");

        UpdateQuestionRequest updateQuestionRequest = (UpdateQuestionRequest) JsonUtils.toObjectFromJson(json, UpdateQuestionRequest.class);
        updateQuestionRequest.setQuestionHeaderText(questionHeader);
        testContext.getScenarioContext().setUpdateQuestionRequest(updateQuestionRequest);
    }

    @Given("^the question state is edited to ' \"([^\"]*)\" '$")
    public void theQuestionStateIsEditedTo(String state) throws Throwable {
        String json = JsonUtils.getJsonInput("question/update_question");

        UpdateQuestionRequest updateQuestionRequest = (UpdateQuestionRequest) JsonUtils.toObjectFromJson(json, UpdateQuestionRequest.class);
        updateQuestionRequest.setQuestionState(state);
        testContext.getScenarioContext().setUpdateQuestionRequest(updateQuestionRequest);
    }

    @When("^the put request to update the question is sent$")
    public void thePutRequestToUpdateTheQuestionIsSent() throws Throwable {
        String json = JsonUtils.toJson(testContext.getScenarioContext().getUpdateQuestionRequest());

        try{
            HttpEntity<String> request = new HttpEntity<>(json, header);
            ResponseEntity<String> response = restTemplate.exchange(baseUrl + ENDPOINT + "/" + onlineHearing.getOnlineHearingId() + "/questions/" + questionIds.get(0),
                    HttpMethod.PUT, request, String.class);

            testContext.getHttpContext().setHttpResponseStatusCode(response.getStatusCodeValue());
            testContext.getHttpContext().setRawResponseString(response.getBody());
        } catch (HttpClientErrorException hsee) {
            testContext.getHttpContext().setHttpResponseStatusCode(hsee.getRawStatusCode());
        }
    }

    @When("^the delete question request is sent$")
    public void the_delete_question_request_is_sent() {

        try {
            Question question = testContext.getScenarioContext().getCurrentQuestion();
            HttpEntity<String> request = new HttpEntity<>("", header);
            ResponseEntity<String> response = restTemplate.exchange(baseUrl + ENDPOINT + "/" + onlineHearing.getOnlineHearingId() + "/questions/" + question.getQuestionId(),
                    HttpMethod.DELETE, request, String.class);

            testContext.getHttpContext().setHttpResponseStatusCode(response.getStatusCodeValue());
            testContext.getHttpContext().setRawResponseString(response.getBody());
        } catch (HttpClientErrorException hsee) {
            testContext.getHttpContext().setHttpResponseStatusCode(hsee.getRawStatusCode());
        }
    }

    @And("^the question body is ' \"([^\"]*)\" '$")
    public void theQuestionBodyIs(String expectedBody) throws IOException {
        QuestionResponse question = (QuestionResponse) JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), QuestionResponse.class);
        assertEquals(expectedBody, question.getQuestionBodyText());
    }

    @And("^the question header is ' \"([^\"]*)\" '$")
    public void theQuestionHeaderIs(String expectedHeader) throws Throwable {
        QuestionResponse question = (QuestionResponse) JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), QuestionResponse.class);
        assertEquals(expectedHeader, question.getQuestionHeaderText());
    }

    @And("^each question in the question round has a correct deadline expiry date$")
    public void eachQuestionInTheQuestionRoundHasADeadlineExpiryDate() throws Throwable {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundResponse questionRoundResponse = (QuestionRoundResponse) JsonUtils.toObjectFromJson(rawJson, QuestionRoundResponse.class);
        List<QuestionResponse> questionResponses = questionRoundResponse.getQuestionList();

        Calendar expectedExpiryDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        expectedExpiryDate.add(Calendar.DAY_OF_YEAR, 7);
        expectedExpiryDate.set(Calendar.HOUR_OF_DAY, 23);
        expectedExpiryDate.set(Calendar.MINUTE, 59);
        expectedExpiryDate.set(Calendar.SECOND, 59);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        List<QuestionResponse> questionsWithNullExpiry = questionResponses.stream()
                .filter(q -> q.getDeadlineExpiryDate() == null )
                .collect(Collectors.toList());

        assertEquals(0, questionsWithNullExpiry.size());

        for (QuestionResponse questionResponse : questionResponses) {
            try {
                assertEquals(expectedExpiryDate.getTime(), df.parse(questionResponse.getDeadlineExpiryDate()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @And("^question history has at least (\\d+) events$")
    public void questionHistoryHasAtLeastEvents(int expectedNumberOfEvents) throws Throwable {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        AllQuestionsResponse allQuestionsResponse = JsonUtils.toObjectFromJson(rawJson, AllQuestionsResponse.class);
        List<QuestionResponse> questions = allQuestionsResponse.getQuestions();

        boolean allMatch = questions.stream()
            .map(questionResponse -> UUID.fromString(questionResponse.getQuestionId()))
            .map(uuid -> questionRepository.findById(uuid))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .allMatch(question -> question.getQuestionStateHistories().size() >= expectedNumberOfEvents);

        assertTrue(allMatch);
    }

    @And("^question (\\d+) contains (\\d+) answer$")
    public void questionContainsAnswer(int questionOrd, int numAnswers) throws Throwable {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        AllQuestionsResponse allQuestionsResponse = JsonUtils.toObjectFromJson(rawJson, AllQuestionsResponse.class);
        List<Answer> answers = allQuestionsResponse.getQuestions().get(questionOrd-1).getAnswers();
        assertEquals(answers.size(), numAnswers);
    }
}
