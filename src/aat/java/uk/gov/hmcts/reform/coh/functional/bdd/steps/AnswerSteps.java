package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerRequest;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.controller.answer.CreateAnswerResponse;
import uk.gov.hmcts.reform.coh.controller.question.CreateQuestionResponse;
import uk.gov.hmcts.reform.coh.controller.question.QuestionRequest;
import uk.gov.hmcts.reform.coh.controller.utils.CohISO8601DateFormat;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohEntityTypes;
import uk.gov.hmcts.reform.coh.functional.bdd.responses.AnswerResponseUtils;
import uk.gov.hmcts.reform.coh.functional.bdd.responses.QuestionResponseUtils;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ContextConfiguration
@SpringBootTest
public class AnswerSteps extends BaseSteps {
    private static final Logger log = LoggerFactory.getLogger(AnswerSteps.class);

    private ResponseEntity<String> response;

    private String endpoint;

    private UUID currentQuestionId;

    private UUID currentAnswerId;

    private Map<String, String> endpoints = new HashMap<String, String>();

    private AnswerRequest answerRequest;

    private OnlineHearing onlineHearing;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private OnlineHearingRepository onlineHearingRepository;

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
    }

    /**
     * Creates a question to be used for testing with an answer
     */
    @Given("^a valid question$")
    public void an_existing_question() throws Exception {
        QuestionRequest questionRequest = JsonUtils.toObjectFromTestName("question/standard_question_v_0_0_5", QuestionRequest.class);

        String onlineHearingCaseId = testContext.getScenarioContext().getCurrentOnlineHearing().getCaseId();
        onlineHearing = onlineHearingRepository.findByCaseId(onlineHearingCaseId).get();
        updateEndpointWithOnlineHearingId();

        ResponseEntity response = sendRequest(CohEntityTypes.QUESTION, HttpMethod.POST.name(), JsonUtils.toJson(questionRequest));
        testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        String json = response.getBody().toString();
        CreateQuestionResponse createQuestionResponse = QuestionResponseUtils.getCreateQuestionResponse(json);
        this.currentQuestionId = createQuestionResponse.getQuestionId();
        testContext.getScenarioContext().addQuestionId(createQuestionResponse.getQuestionId());
        testContext.getScenarioContext().setCurrentQuestion(QuestionResponseUtils.getQuestion(createQuestionResponse));
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
            
            // For missing submitting answer to non-existing answer tests
            UUID questionId = Optional.ofNullable(testContext.getScenarioContext().getCurrentQuestion())
                    .flatMap(question -> Optional.ofNullable(question.getQuestionId()))
                    .orElse(UUID.randomUUID());

            endpoint = endpoint.replaceAll("question_id", questionId.toString());
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
            List<UUID> answerIds = testContext.getScenarioContext().getAnswerIds();
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

    @When("^a (.*) request is sent for an answer$")
    public void aRequestIsSentForAnswer(String method) throws Exception {

        String json = JsonUtils.toJson(answerRequest);
        try {
            ResponseEntity response = sendRequest(CohEntityTypes.ANSWER, method, json);
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);

            if (HttpMethod.POST.name().equalsIgnoreCase(method)) {
                testContext.getScenarioContext().setCurrentAnswer(AnswerResponseUtils.getAnswer(getCreateAnswerResponse()));
            }
        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(hcee);
        }
    }

    private CreateAnswerResponse getCreateAnswerResponse() throws Exception {
        String json = testContext.getHttpContext().getRawResponseString();
        return JsonUtils.toObjectFromJson(json, CreateAnswerResponse.class);
    }

    @Then("^there are (\\d+) answers$")
    public void there_are_count_answers(int count) throws Throwable {
        String json = response.getBody();
        Answer[] myObjects = JsonUtils.toObjectFromJson(json, Answer[].class);

        assertEquals("Response status code", myObjects.length, count);
    }

    @Then("^the answer response answer text is '(.*)'$")
    public void the_answer_text_is(String text) throws Throwable {
        assertEquals("Answer text", text, getAnswerResponse().getAnswerText());
    }

    @Then("^the answer response answer state is '(.*)'$")
    public void the_answer_state_is(String text) throws Throwable {
        assertEquals("Answer state name", text, getAnswerResponse().getStateResponse().getName());
    }


    @Then("^the answer response answer state datetime is a valid ISO8601 date$")
    public void the_answer_state_datetime_is_iso8601() throws Throwable {
        AnswerResponse response = getAnswerResponse();

        try {
            CohISO8601DateFormat.parse(response.getStateResponse().getDatetime());
            assertTrue(true);
        }
        catch (Exception e) {
            assertTrue(false);
        }
    }

    private AnswerResponse getAnswerResponse() throws Exception {
        String json = testContext.getHttpContext().getRawResponseString();
        return JsonUtils.toObjectFromJson(json, AnswerResponse.class);
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