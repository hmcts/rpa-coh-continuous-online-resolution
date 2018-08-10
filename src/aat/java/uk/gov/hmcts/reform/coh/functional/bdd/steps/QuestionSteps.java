package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.controller.question.*;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundResponse;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundsResponse;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionStateHistory;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohEntityTypes;
import uk.gov.hmcts.reform.coh.functional.bdd.responses.QuestionResponseUtils;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static uk.gov.hmcts.reform.coh.utils.JsonUtils.*;

@ContextConfiguration
@SpringBootTest
public class QuestionSteps extends BaseSteps {

    private String ENDPOINT = "/continuous-online-hearings";
    private Question question;
    private QuestionRequest questionRequest;
    private List<UUID> questionIds;
    private boolean allQuestionRounds;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    public QuestionSteps(TestContext testContext) {
        super(testContext);
    }

    @Before
    public void setup() throws Exception {
        super.setup();
        questionIds = new ArrayList<>();
    }

    @And("^the post request is sent to create the question$")
    public void theDraftAQuestion() throws Exception {
        try{
            ResponseEntity response = sendRequest(CohEntityTypes.QUESTION, HttpMethod.POST.name(), toJson(questionRequest));
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
            CreateQuestionResponse createQuestionResponse = QuestionResponseUtils.getCreateQuestionResponse(response.getBody().toString());
            questionIds.add(createQuestionResponse.getQuestionId());
            testContext.getScenarioContext().setCurrentQuestion(QuestionResponseUtils.getQuestion(createQuestionResponse));
        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(hcee);
        }
    }

    @And("^the get request is sent to retrieve all questions$")
    public void getAllQuestionsForOnlineHearing() {
        try {
            HttpEntity<String> request = new HttpEntity<>("", header);
            ResponseEntity<String> response = getRestTemplate().exchange(getAllQuestionsEndpoint(), HttpMethod.GET, request, String.class);

           testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(hcee);
        }
    }

    @And("^the get request is sent to retrieve the submitted question$")
    public void getTheSubmittedQuestion() {
        try {
            ResponseEntity<String> response = sendRequest(CohEntityTypes.QUESTION, HttpMethod.GET.name(), "");
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(hcee);
        }
    }

    @Given("^a standard question")
    public void aStandardQuestionRound() throws IOException{
        questionRequest = toObjectFromTestName("question/standard_question_v_0_0_5", QuestionRequest.class);
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
    public void theGetRequestIsSentToGetAllQuestionRounds() throws Exception {
        HttpEntity<String> request = new HttpEntity<>("", header);
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        ResponseEntity<String> response = getRestTemplate().exchange(baseUrl + ENDPOINT + "/" + onlineHearing.getOnlineHearingId() + "/questionrounds", HttpMethod.GET, request, String.class);

        testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);

        allQuestionRounds = true;
    }

    @When("^the get request is sent to get question round ' \"([^\"]*)\" '$")
    public void theGetRequestIsSentToGetQuestionRound(int questionRoundN) throws Exception {
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();

        HttpEntity<String> request = new HttpEntity<>("", header);
        ResponseEntity<String> response = getRestTemplate().exchange(baseUrl + ENDPOINT + "/" + onlineHearing.getOnlineHearingId() + "/questionrounds/" + questionRoundN, HttpMethod.GET, request, String.class);

        testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);

        allQuestionRounds = false;
    }

    @When("^the put request is sent to issue the question round ' \"([^\"]*)\" '$")
    public void thePutRequestIsSentToQuestionRound(int questionRoundN) throws Throwable {
        String json = getJsonInput("question_round/issue_question_round");

        try {
            OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
            HttpEntity<String> request = new HttpEntity<>(json, header);
            ResponseEntity<String> response = getRestTemplate().exchange(baseUrl + ENDPOINT + "/" + onlineHearing.getOnlineHearingId() + "/questionrounds/" + questionRoundN,
                    HttpMethod.PUT, request, String.class);
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        } catch (HttpClientErrorException hsee) {
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(hsee);
        }
    }

    @And("^the question round ' \"([^\"]*)\" ' is ' \"([^\"]*)\" '$")
    public void theQuestionRoundIs(int questionRoundNumber, String expectedState) throws IOException {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundResponse questionRoundResponse;

        if(allQuestionRounds) {
            QuestionRoundsResponse questionRoundsResponse = toObjectFromJson(rawJson, QuestionRoundsResponse.class);
            questionRoundResponse = questionRoundsResponse.getQuestionRounds().get(questionRoundNumber - 1);
        }else{
            questionRoundResponse = toObjectFromJson(rawJson, QuestionRoundResponse.class);

        }
        assertTrue(questionRoundResponse.getQuestionRoundState().getState().equalsIgnoreCase(expectedState));
    }

    @And("^the number of questions rounds is ' \"([^\"]*)\" '$")
    public void theNumberOfQuestionsRoundsIs(int expectedQuestionRounds) throws IOException {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundsResponse questionRoundsResponse = toObjectFromJson(rawJson, QuestionRoundsResponse.class);
        int questionRounds = questionRoundsResponse.getQuestionRounds().size();

        assertEquals(expectedQuestionRounds, questionRounds);
    }

    @And("^the previous question round is ' \"([^\"]*)\" '$")
    public void thePreviousQuestionRoundIs(int expectedPreviousQuestionRound) throws Throwable {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundsResponse questionRoundsResponse = toObjectFromJson(rawJson, QuestionRoundsResponse.class);
        int previousQuestionRound = questionRoundsResponse.getPreviousQuestionRound();

        assertEquals(expectedPreviousQuestionRound, previousQuestionRound);
    }

    @And("^the current question round is ' \"([^\"]*)\" '$")
    public void theCurrentQuestionRoundIs(int expectedCurrentQuestionRound) throws Throwable {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundsResponse questionRoundsResponse = toObjectFromJson(rawJson, QuestionRoundsResponse.class);
        int currentQuestionRound = questionRoundsResponse.getCurrentQuestionRound();

        assertEquals(expectedCurrentQuestionRound, currentQuestionRound);
    }

    @And("^the next question round is ' \"([^\"]*)\" '$")
    public void theNextQuestionRoundIs(int expectedNextQuestionRound) throws Throwable {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundsResponse questionRoundsResponse = toObjectFromJson(rawJson, QuestionRoundsResponse.class);
        int nextQuestionRound = questionRoundsResponse.getCurrentQuestionRound();

        assertEquals(expectedNextQuestionRound, nextQuestionRound);
    }

    @And("^the max question round is ' \"([^\"]*)\" '$")
    public void theMaxQuestionRoundIs(int expectedMaxQuestionRound) throws Throwable {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundsResponse questionRoundsResponse = toObjectFromJson(rawJson, QuestionRoundsResponse.class);
        int maxQuestionRound = questionRoundsResponse.getCurrentQuestionRound();

        assertEquals(expectedMaxQuestionRound, maxQuestionRound);
    }

    @And("^the number of questions in question round ' \"([^\"]*)\" ' is ' \"([^\"]*)\" '$")
    public void theNumberOfQuestionsInQuestionRoundIs(int questionRoundN, int expectedQuestions) throws Throwable {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundResponse questionRound;

        if (allQuestionRounds) {
            QuestionRoundsResponse questionRoundsResponse = toObjectFromJson(rawJson, QuestionRoundsResponse.class);
            questionRound = questionRoundsResponse.getQuestionRounds().get(questionRoundN - 1);
        }else{
            questionRound = toObjectFromJson(rawJson, QuestionRoundResponse.class);
        }
        assertEquals(expectedQuestions, questionRound.getQuestionList().size());
    }

    @And("^the response contains (\\d) questions$")
    public void the_response_contains_n_questions(int count) throws Throwable {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        AllQuestionsResponse questionResponses = toObjectFromJson(rawJson, AllQuestionsResponse.class);
        assertEquals(count, questionResponses.getQuestions().size());
    }

    @And("^the question id matches$")
    public void the_question_id_matches() throws Throwable {
        QuestionResponse question = toObjectFromJson(testContext.getHttpContext().getRawResponseString(), QuestionResponse.class);
        assertEquals(testContext.getScenarioContext().getCurrentQuestion().getQuestionId().toString(), question.getQuestionId());
    }

    @And("^the question state name is (.*)$")
    public void the_question_state_name_is(String stateName) throws Throwable {
        QuestionResponse question = toObjectFromJson(testContext.getHttpContext().getRawResponseString(), QuestionResponse.class);
        assertEquals(stateName, question.getCurrentState().getName());
    }

    @And("^the question state timestamp is today$")
    public void the_question_state_timestamp_is_today() throws Throwable {
        QuestionResponse question = toObjectFromJson(testContext.getHttpContext().getRawResponseString(), QuestionResponse.class);
        assertTrue(question.getCurrentState().getDatetime().contains(df.format(new Date())));
    }

    @And("^each question in the question round has a history of at least ' \"(\\d)\" ' events$")
    public void eachQuestionInTheQuestionRoundHasHistory(int histories) throws Throwable {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundResponse questionRoundResponse = toObjectFromJson(rawJson, QuestionRoundResponse.class);
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
        String json = getJsonInput("question/update_question");

        UpdateQuestionRequest updateQuestionRequest = toObjectFromJson(json, UpdateQuestionRequest.class);
        updateQuestionRequest.setQuestionBodyText(questionBody);
        testContext.getScenarioContext().setUpdateQuestionRequest(updateQuestionRequest);
    }

    @Given("^the question header is edited to ' \"([^\"]*)\" '$")
    public void theQuestionHeaderIsEditedTo(String questionHeader) throws Throwable {
        String json = getJsonInput("question/update_question");

        UpdateQuestionRequest updateQuestionRequest = toObjectFromJson(json, UpdateQuestionRequest.class);
        updateQuestionRequest.setQuestionHeaderText(questionHeader);
        testContext.getScenarioContext().setUpdateQuestionRequest(updateQuestionRequest);
    }

    @Given("^the question state is edited to ' \"([^\"]*)\" '$")
    public void theQuestionStateIsEditedTo(String state) throws Throwable {
        String json = getJsonInput("question/update_question");

        UpdateQuestionRequest updateQuestionRequest = toObjectFromJson(json, UpdateQuestionRequest.class);
        updateQuestionRequest.setQuestionState(state);
        testContext.getScenarioContext().setUpdateQuestionRequest(updateQuestionRequest);
    }

    @When("^the put request to update the question is sent$")
    public void thePutRequestToUpdateTheQuestionIsSent() throws Throwable {
        try {
            String json = toJson(testContext.getScenarioContext().getUpdateQuestionRequest());
            ResponseEntity<String> response = sendRequest(CohEntityTypes.QUESTION, HttpMethod.PUT.name(), json);
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(hcee);
        }
    }

    @When("^the delete question request is sent$")
    public void the_delete_question_request_is_sent() throws Exception {

        try {
            OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
            Question question = testContext.getScenarioContext().getCurrentQuestion();
            HttpEntity<String> request = new HttpEntity<>("", header);
            ResponseEntity<String> response = getRestTemplate().exchange(baseUrl + ENDPOINT + "/" + onlineHearing.getOnlineHearingId() + "/questions/" + question.getQuestionId(),
                    HttpMethod.DELETE, request, String.class);

            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(hcee);
        }
    }

    @And("^the question body is ' \"([^\"]*)\" '$")
    public void theQuestionBodyIs(String expectedBody) throws IOException {
        QuestionResponse question = toObjectFromJson(testContext.getHttpContext().getRawResponseString(), QuestionResponse.class);
        assertEquals(expectedBody, question.getQuestionBodyText());
    }

    @And("^the question header is ' \"([^\"]*)\" '$")
    public void theQuestionHeaderIs(String expectedHeader) throws Throwable {
        QuestionResponse question = toObjectFromJson(testContext.getHttpContext().getRawResponseString(), QuestionResponse.class);
        assertEquals(expectedHeader, question.getQuestionHeaderText());
    }

    @And("^each question in the question round has a correct deadline expiry date$")
    public void eachQuestionInTheQuestionRoundHasADeadlineExpiryDate() throws Throwable {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        QuestionRoundResponse questionRoundResponse = toObjectFromJson(rawJson, QuestionRoundResponse.class);
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
        AllQuestionsResponse allQuestionsResponse = toObjectFromJson(rawJson, AllQuestionsResponse.class);
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
        AllQuestionsResponse allQuestionsResponse = toObjectFromJson(rawJson, AllQuestionsResponse.class);
        List<AnswerResponse> answers = allQuestionsResponse.getQuestions().get(questionOrd-1).getAnswers();
        assertEquals(answers.size(), numAnswers);
    }

    @Then("^the question round has a state of '(.*)'$")
    public void theQuestionRoundHasAStateOf(String qrState) throws Exception {
        assertEquals(qrState, getQuestionRoundResponse().getQuestionRoundState().getState());
    }

    @And("^the question round has (\\d+) question$")
    public void theQuestionRoundHasNQuestion(int count) throws Exception {
        assertEquals(count, getQuestionRoundResponse().getQuestionList().size());
    }

    @And("^all questions in the question round have a state of '(.*)'$")
    public void allQuestionsInTheQuestionsRoundHaveAStateOf(String state) throws Exception {
        allQuestionsHaveAStateOf(getQuestionRoundResponse().getQuestionList(), state);
    }

    @And("^all questions in the question rounds have a state of '(.*)'$")
    public void allQuestionsInTheQuestionsRoundsHaveAStateOf(String state) throws Exception {
        getQuestionRoundsResponse().getQuestionRounds().forEach(qr -> allQuestionsHaveAStateOf(qr.getQuestionList(), state));
    }

    private void allQuestionsHaveAStateOf(List<QuestionResponse> responses, String state) {
        responses.forEach(q -> assertEquals(state, q.getCurrentState().getName()));
    }

    @And("^all questions in the question round have an answer$")
    public void allQuestionsInTheQuestionRoundHaveAnAnswer() throws Exception {
        nQuestionsInTheQuestionRoundHaveNAnswer(getQuestionRoundResponse().getQuestionList(), getQuestionRoundResponse().getQuestionList().size());
    }

    @And("^(\\d+) questions in the question round has an answer$")
    public void nQuestionsInTheQuestionRoundHaveAnAnswer(int count) throws Exception {
        nQuestionsInTheQuestionRoundHaveNAnswer(getQuestionRoundResponse().getQuestionList(), count);
    }

    @And("^all questions in the question rounds have an answer$")
    public void allQuestionsInTheQuestionRoundsHaveAnAnswer() throws Exception {
        getQuestionRoundsResponse()
                .getQuestionRounds()
                .forEach(qr ->
                        nQuestionsInTheQuestionRoundHaveNAnswer(qr.getQuestionList(), qr.getQuestionList().size())
                );
    }

    public void nQuestionsInTheQuestionRoundHaveNAnswer(List<QuestionResponse> questions, int count)  {
        assertEquals(count, questions.stream().filter(q -> !(q.getAnswers() == null || q.getAnswers().isEmpty())).count());
    }

    @And("^(\\d+) question in the question round has a state of '(.*)'$")
    public void questionInTheQuestionRoundHasAStateOfQuestion_answered(int count, String state) throws Exception {
        assertEquals(count, getQuestionRoundResponse().getQuestionList().stream().filter(q -> q.getCurrentState().getName().equals(state)).count());
    }

    private QuestionRoundResponse getQuestionRoundResponse() throws Exception {

        String rawJson = testContext.getHttpContext().getRawResponseString();
        return toObjectFromJson(rawJson, QuestionRoundResponse.class);
    }

    private QuestionRoundsResponse getQuestionRoundsResponse() throws Exception {

        String rawJson = testContext.getHttpContext().getRawResponseString();
        return toObjectFromJson(rawJson, QuestionRoundsResponse.class);
    }

    private String getAllQuestionsEndpoint() {
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        return baseUrl + CohUriBuilder.buildQuestionPost(onlineHearing.getOnlineHearingId());
    }
}
