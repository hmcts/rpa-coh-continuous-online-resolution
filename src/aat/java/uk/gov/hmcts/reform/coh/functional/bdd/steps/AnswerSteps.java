package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerRequest;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.controller.answer.CreateAnswerResponse;
import uk.gov.hmcts.reform.coh.controller.question.CreateQuestionResponse;
import uk.gov.hmcts.reform.coh.controller.question.QuestionRequest;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingPanelMemberRepository;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ContextConfiguration
@SpringBootTest
public class AnswerSteps extends BaseSteps{
    private static final Logger log = LoggerFactory.getLogger(AnswerSteps.class);

    private ResponseEntity<String> response;

    private String endpoint;

    private UUID currentQuestionId;

    private UUID currentAnswerId;

    private Map<String, String> endpoints = new HashMap<String, String>();

    private AnswerRequest answerRequest;

    private List<UUID> questionIds;

    private List<UUID> answerIds;

    private OnlineHearing onlineHearing;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private OnlineHearingRepository onlineHearingRepository;

    @Autowired
    private OnlineHearingPanelMemberRepository onlineHearingPanelMemberRepository;

    @Autowired
    public AnswerSteps(TestContext testContext) {
        super(testContext);
    }

    @Before
    public void setUp() throws Exception {
        super.setup();
        endpoints.put("answer", "/continuous-online-hearings/onlineHearing_id/questions/question_id/answers");
        endpoints.put("question", "/continuous-online-hearings/onlineHearing_id/questions");

        currentQuestionId = null;
        currentAnswerId = null;

        questionIds = new ArrayList<>();
        answerIds = new ArrayList<>();
    }

    @After
    public void cleanUp() {
        /**
         * For each test run, answers are attached to questions. These need
         * to be deleted after test completion. Delete in reverse order for
         * FK constrains
         */
        for (UUID answerId : answerIds) {
            answerService.deleteAnswer(new Answer().answerId(answerId));
        }

        for (UUID questionId : questionIds) {
            Optional<Question> question = questionService.retrieveQuestionById(questionId);
            if (question.isPresent()) {
                questionService.deleteQuestion(question.get());
            }
        }
    }

    /**
     * Creates a question to be used for testing with an answer
     */
    @Given("^a valid question$")
    public void an_existing_question() throws IOException {
        QuestionRequest questionRequest = JsonUtils.toObjectFromTestName("question/standard_question_v_0_0_5", QuestionRequest.class);

        String onlineHearingCaseId = testContext.getScenarioContext().getCurrentOnlineHearing().getCaseId();
        onlineHearing = onlineHearingRepository.findByCaseId(onlineHearingCaseId).get();
        updateEndpointWithOnlineHearingId();

        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(JsonUtils.toJson(questionRequest), header);
        response = restTemplate.exchange(baseUrl + endpoints.get("question"), HttpMethod.POST, request, String.class);
        String json = response.getBody();
        CreateQuestionResponse createQuestionResponse = JsonUtils.toObjectFromJson(json, CreateQuestionResponse.class);
        this.currentQuestionId = createQuestionResponse.getQuestionId();
        questionIds.add(createQuestionResponse.getQuestionId());
    }

    @Given("^a standard answer$")
    public void a_standard_answer() throws IOException {
        this.answerRequest = JsonUtils.toObjectFromTestName("answer/standard_answer", AnswerRequest.class);
        String onlineHearingCaseId = testContext.getScenarioContext().getCurrentOnlineHearing().getCaseId();
        onlineHearing = onlineHearingRepository.findByCaseId(onlineHearingCaseId).get();
        updateEndpointWithOnlineHearingId();
    }

    @Given("^a valid answer$")
    public void a_valid_answer() {
        answer_text_is("foo");
    }

    @Given("^the answer text is empty$")
    public void answer_text_is_empty() {
        answer_text_is(null);
    }

    @Given("^the answer text is '(.*)'$")
    public void answer_text_is(String text) {
        answerRequest.setAnswerText(text);
    }

    @Given("^the answer state is (.*)$")
    public void answer_state_is(String state) {
        answerRequest.setAnswerState(state);
    }

    @Given("^an unknown answer identifier$")
    public void an_unknown_answer_identifier$() throws Throwable {
        currentAnswerId = UUID.randomUUID();
    }

    @Given("^the endpoint is for submitting an (.*)$")
    public void the_endpoint_is_for_submitting_an_answer(String entity) {
        if (endpoints.containsKey(entity)) {
            // See if we need to fix the endpoint
            this.endpoint = endpoints.get(entity);
            String questionId = testContext.getScenarioContext().getCurrentQuestion().getQuestionId().toString();
            endpoint = endpoint.replaceAll("question_id", questionId);
        }

        if ("answer".equalsIgnoreCase(entity) && currentAnswerId != null) {
            endpoint += "/" + currentAnswerId;
        }
    }

    @Given("^the endpoint is for retrieving an (.*)$")
    public void the_endpoint_is_for_retrieving_an_answer(String entity) {
        if (endpoints.containsKey(entity)) {
            // See if we need to fix the endpoint
            this.endpoint = endpoints.get(entity);
            endpoint = endpoint.replaceAll("question_id", currentQuestionId == null ? UUID.randomUUID().toString() : currentQuestionId.toString());
        }

        if ("answer".equalsIgnoreCase(entity)) {
            UUID currentAnswerId = answerIds.get(answerIds.size() - 1);
            endpoint += "/" + currentAnswerId;
        }
    }

    @Given("^the endpoint is for submitting all (.*)$")
    public void the_endpoint_is_for_submitting_all_answer(String entity) {
        if (endpoints.containsKey(entity)) {
            // See if we need to fix the endpoint
            this.endpoint = endpoints.get(entity);
            endpoint = endpoint.replaceAll("question_id", currentQuestionId == null ? "0" : currentQuestionId.toString());
        }
    }

    @Given("^an update to the answer is required$")
    public void an_update_to_the_answer_is_required() {
        try {
            CreateAnswerResponse answerResponse = JsonUtils.toObjectFromJson(response.getBody().toString(), CreateAnswerResponse.class);
            this.endpoint = endpoint + "/" + answerResponse.getAnswerId();
        } catch (Exception e) {
            log.error("Exception " + e.getMessage());
        }
    }

    @When("^a (.*) request is sent$")
    public void send_request(String type) throws IOException {

        String json = JsonUtils.toJson(answerRequest);

        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");

        int httpResponseCode = 0;
        RestTemplate restTemplate = getRestTemplate();
        try {
            if ("GET".equalsIgnoreCase(type)) {
                HttpEntity<String> request = new HttpEntity<>("", header);
                response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.GET, request, String.class);
            } else if ("POST".equalsIgnoreCase(type)) {
                HttpEntity<String> request = new HttpEntity<>(json, header);
                response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.POST, request, String.class);
            } else if ("PUT".equalsIgnoreCase(type)) {
                /**
                 * This is a workaround for https://jira.spring.io/browse/SPR-15347
                 *
                 **/
                HttpEntity<String> request = new HttpEntity<>(json, header);
                response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, request, String.class);
            }
            httpResponseCode = response.getStatusCodeValue();

            Optional<UUID> optAnswerId = getAnswerId(response.getBody());
            if (optAnswerId.isPresent()) {
                answerIds.add(optAnswerId.get());
            }
        } catch (HttpClientErrorException hcee) {
            httpResponseCode = hcee.getRawStatusCode();
        }
        testContext.getHttpContext().setHttpResponseStatusCode(httpResponseCode);
    }

    @Then("^there are (\\d+) answers$")
    public void there_are_count_answers(int count) throws Throwable {
        String json = response.getBody();
        Answer[] myObjects = JsonUtils.toObjectFromJson(json, Answer[].class);

        assertEquals("Response status code", myObjects.length, count);
    }

    @Then("^the answer response answer text is '(.*)'$")
    public void the_answer_text_is(String text) throws Throwable {
        String json = response.getBody();
        AnswerResponse response = JsonUtils.toObjectFromJson(json, AnswerResponse.class);

        assertEquals("Answer text", text, response.getAnswerText());
    }

    @Then("^the answer response answer state is '(.*)'$")
    public void the_answer_state_is(String text) throws Throwable {
        String json = response.getBody();
        AnswerResponse response = JsonUtils.toObjectFromJson(json, AnswerResponse.class);

        assertEquals("Answer state name", text, response.getStateResponse().getName());
    }


    @Then("^the answer response answer state datetime is a valid ISO8601 date$")
    public void the_answer_state_datetime_is_iso8601() throws Throwable {
        String json = response.getBody();
        AnswerResponse response = JsonUtils.toObjectFromJson(json, AnswerResponse.class);

        try {
            ISO8601DateFormat df = new ISO8601DateFormat();
            df.parse(response.getStateResponse().getDatetime());
            assertTrue(true);
        }
        catch (Exception e) {
            assertTrue(false);
        }
    }

    /**
     * This will work out which type of entity has been returned and get the
     * answer id from it
     *
     * @param json
     * @return
     */
    private Optional<UUID> getAnswerId(String json) throws IOException {

        if (json == null) {
            return Optional.empty();
        }

        UUID answerId = null;
        if ((json.indexOf("question_id") > 0) || json.contains("current_answer_state")) {
            AnswerResponse answer = JsonUtils.toObjectFromJson(json, AnswerResponse.class);
            answerId = UUID.fromString(answer.getAnswerId());
        } else {

            if (!json.startsWith("[")) {
                CreateAnswerResponse answerResponse = JsonUtils.toObjectFromJson(json, CreateAnswerResponse.class);
                answerId = answerResponse.getAnswerId();
            }
        }

        return Optional.ofNullable(answerId);
    }

    private void updateEndpointWithOnlineHearingId(){
        endpoints.put("question",endpoints.get("question").replaceAll("onlineHearing_id", String.valueOf(onlineHearing.getOnlineHearingId())));
        endpoints.put("answer",endpoints.get("answer").replaceAll("onlineHearing_id", String.valueOf(onlineHearing.getOnlineHearingId())));
    }
}