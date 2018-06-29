package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
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
import uk.gov.hmcts.reform.coh.controller.answer.AnswerRequest;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestTrustManager;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.QuestionService;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.junit.Assert.assertEquals;

@ContextConfiguration
@SpringBootTest
public class AnswerSteps extends BaseSteps{

    private RestTemplate restTemplate;

    private ResponseEntity<String> response;

    private String endpoint;

    private Long currentQuestionId;

    private Long currentAnswerId;

    private Map<String, String> endpoints = new HashMap<String, String>();

    private AnswerRequest answerRequest;

    private List<Long> questionIds;

    private List<Long> answerIds;
    private String onlineHearingExternalRef;
    private OnlineHearing onlineHearing;
    private int httpResponseCode;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private OnlineHearingRepository onlineHearingRepository;

    @Before
    public void setup() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        restTemplate = new RestTemplate(TestTrustManager.getInstance().getTestRequestFactory());
        endpoints.put("answer", "/online-hearings/onlineHearing_id/questions/question_id/answers");
        endpoints.put("question", "/online-hearings/onlineHearing_id/questions");

        currentQuestionId = null;
        currentAnswerId = null;

        questionIds = new ArrayList<>();
        answerIds = new ArrayList<>();

        OnlineHearing preparedOnlineHearing = (OnlineHearing)JsonUtils.toObjectFromTestName("create_online_hearing", OnlineHearing.class);
        onlineHearingExternalRef = preparedOnlineHearing.getExternalRef();
    }

    @After
    public void cleanup() {
        /**
         * For each test run, answers are attached to questions. These need
         * to be deleted after test completion. Delete in reverse order for
         * FK constrains
         */
        for (Long answerId : answerIds) {
            answerService.deleteAnswer(new Answer().answerId(answerId));
        }

        for (Long questionId : questionIds) {
            questionService.deleteQuestion(new Question().questionId(questionId));
        }

        try {
            onlineHearingRepository.deleteByExternalRef(onlineHearingExternalRef);
        } catch(DataIntegrityViolationException e){
            System.out.println("Failure may be due to foreign key. This is okay because the online hearing will be deleted elsewhere." + e);
        }
    }

    /**
     * Creates a question to be used for testing with an answer
     */
    @Given("^a valid question$")
    public void an_existing_question() throws IOException {
        Question question = new Question();
        question.setSubject("foo");
        question.setQuestionText("question text");
        question.setQuestionRound(1);

        onlineHearing = onlineHearingRepository.findByExternalRef(onlineHearingExternalRef).get();
        updateEndpointWithOnlineHearingId();

        question.setOnlineHearing(onlineHearing);

        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(JsonUtils.toJson(question), header);
        response = restTemplate.exchange(baseUrl + endpoints.get("question"), HttpMethod.POST, request, String.class);
        String json = response.getBody();
        question = (Question) JsonUtils.toObjectFromJson(json, Question.class);
        this.currentQuestionId = question.getQuestionId();
        questionIds.add(question.getQuestionId());
    }

    @Given("^a standard answer$")
    public void a_standard_answer() throws IOException {
        JsonUtils utils = new JsonUtils();
        this.answerRequest = (AnswerRequest)utils.toObjectFromTestName("answer/standard_answer", AnswerRequest.class);
        onlineHearing = onlineHearingRepository.findByExternalRef(onlineHearingExternalRef).get();
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

    @Given("^an unknown answer identifier$")
    public void an_unknown_answer_identifier$() throws Throwable {
        currentAnswerId = 0L;
    }

    @Given("^the endpoint is for submitting an (.*)$")
    public void the_endpoint_is_for_submitting_an_answer(String entity) throws Throwable {
        if (endpoints.containsKey(entity)) {
            // See if we need to fix the endpoint
            this.endpoint = endpoints.get(entity);
            endpoint = endpoint.replaceAll("question_id", currentQuestionId == null ? "0" : currentQuestionId.toString());
        }

        if ("answer".equalsIgnoreCase(entity) && currentAnswerId != null) {
            endpoint += "/" + currentAnswerId;
        }
    }

    @Given("^the endpoint is for submitting all (.*)$")
    public void the_endpoint_is_for_submitting_all_answer(String entity) throws Throwable {
        if (endpoints.containsKey(entity)) {
            // See if we need to fix the endpoint
            this.endpoint = endpoints.get(entity);
            endpoint = endpoint.replaceAll("question_id", currentQuestionId == null ? "0" : currentQuestionId.toString());
        }
    }

    @Given("^an update to the answer is required$")
    public void an_update_to_the_answer_is_required() {
        try {
            AnswerResponse answerResponse = (AnswerResponse) JsonUtils.toObjectFromJson(response.getBody().toString(), AnswerResponse.class);
            this.endpoint = endpoint + "/" + answerResponse.getAnswerId();
        } catch (Exception e) {
            System.out.println("Exception " + e.getMessage());
        }
    }

    @And("^the answer state is '(.*)'$")
    public void theAnswerStateIsSUBMITTED(String answerState) throws Throwable {
        answerRequest.setAnswerText(answerState);
    }

    @When("^a (.*) request is sent$")
    public void send_request(String type) throws IOException {

        String json = JsonUtils.toJson(answerRequest);

        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");

        try {
            if ("GET".equalsIgnoreCase(type)) {
                response = restTemplate.getForEntity(baseUrl + endpoint, String.class);
            } else if ("POST".equalsIgnoreCase(type)) {
                HttpEntity<String> request = new HttpEntity<>(json, header);
                response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.POST, request, String.class);
            } else if ("PATCH".equalsIgnoreCase(type)) {
                /**
                 * This is a workaround for https://jira.spring.io/browse/SPR-15347
                 *
                 **/
                HttpEntity<String> request = new HttpEntity<>(json, header);
                response = restTemplate.exchange(baseUrl + endpoint + "?_method=patch", HttpMethod.POST, request, String.class);
            }
            httpResponseCode = response.getStatusCodeValue();


            Optional<Long> optAnswerId = getAnswerId(response.getBody());
            if (optAnswerId.isPresent()) {
                answerIds.add(optAnswerId.get());
            }
        } catch (HttpClientErrorException hcee) {
            httpResponseCode = hcee.getRawStatusCode();
        }
    }

    @Then("^the response code is (\\d+)$")
    public void the_response_code_is(int responseCode) throws Throwable {
        assertEquals("Response status code", responseCode, httpResponseCode);
    }

    @Then("^there are (\\d+) answers$")
    public void there_are_count_answers(int count) throws Throwable {
        String json = response.getBody();
        Answer[] myObjects = (Answer[]) JsonUtils.toObjectFromJson(json, Answer[].class);

        assertEquals("Response status code", myObjects.length, count);
    }

    /**
     * This will work out which type of entity has been returned and get the
     * answer id from it
     *
     * @param json
     * @return
     */
    private Optional<Long> getAnswerId(String json) throws IOException {

        if (json == null) {
            return Optional.empty();
        }

        Long answerId = null;
        if (json.indexOf("question_id") > 0) {
            Answer answer = (Answer) JsonUtils.toObjectFromJson(json, Answer.class);
            answerId = answer.getAnswerId();
        } else {
            if (!json.startsWith("[")) {
                AnswerResponse answerResponse = (AnswerResponse) JsonUtils.toObjectFromJson(json, AnswerResponse.class);
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