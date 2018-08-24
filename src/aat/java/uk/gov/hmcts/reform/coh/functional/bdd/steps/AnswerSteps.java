package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
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
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohEntityTypes;
import uk.gov.hmcts.reform.coh.functional.bdd.responses.AnswerResponseUtils;
import uk.gov.hmcts.reform.coh.functional.bdd.responses.QuestionResponseUtils;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ContextConfiguration
@SpringBootTest
public class AnswerSteps extends BaseSteps {

    private AnswerRequest answerRequest;

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
    }

    /**
     * Creates a question to be used for testing with an answer
     */
    @Given("^a valid question$")
    public void anExistingQuestion() throws Exception {
        QuestionRequest questionRequest = JsonUtils.toObjectFromTestName("question/standard_question_v_0_0_5", QuestionRequest.class);

        ResponseEntity response = sendRequest(CohEntityTypes.QUESTION, HttpMethod.POST.name(), JsonUtils.toJson(questionRequest));
        testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        String json = response.getBody().toString();
        CreateQuestionResponse createQuestionResponse = QuestionResponseUtils.getCreateQuestionResponse(json);
        testContext.getScenarioContext().addQuestionId(createQuestionResponse.getQuestionId());
        testContext.getScenarioContext().setCurrentQuestion(QuestionResponseUtils.getQuestion(createQuestionResponse));
    }

    @Given("^a standard answer$")
    public void a_standard_answer() throws IOException {
        this.answerRequest = JsonUtils.toObjectFromTestName("answer/standard_answer", AnswerRequest.class);
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
        Answer answer = new Answer();
        answer.setAnswerId(UUID.randomUUID());
        testContext.getScenarioContext().setCurrentAnswer(answer);
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

    private CreateAnswerResponse getCreateAnswerResponse() throws Exception {
        String json = testContext.getHttpContext().getRawResponseString();
        return JsonUtils.toObjectFromJson(json, CreateAnswerResponse.class);
    }

    private AnswerResponse getAnswerResponse() throws Exception {
        String json = testContext.getHttpContext().getRawResponseString();
        return JsonUtils.toObjectFromJson(json, AnswerResponse.class);
    }
}