package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.PendingException;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.DecisionReplyResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.*;
import uk.gov.hmcts.reform.coh.controller.question.QuestionResponse;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;
import java.util.UUID;

import static junit.framework.TestCase.*;
import static org.junit.Assert.assertNull;

public class OnlineHearingSteps extends BaseSteps {

    private UpdateOnlineHearingRequest request;

    @Autowired
    public OnlineHearingSteps(TestContext testContext) {
        super(testContext);
    }

    @Before
    public void setup() throws Exception {
        super.setup();
    }

    @Given("^a standard online hearing$")
    public void a_standard_online_hearing() throws IOException {
        OnlineHearingRequest onlineHearingRequest = JsonUtils
            .toObjectFromTestName("online_hearing/standard_online_hearing", OnlineHearingRequest.class);
        testContext.getScenarioContext().setCurrentOnlineHearingRequest(onlineHearingRequest);
    }

    @Given("^the case id is '(.*)'")
    public void the_case_id_is(String caseId) {
        testContext.getScenarioContext().getCurrentOnlineHearingRequest().setCaseId(caseId);
    }

    @When("^a (.*) request is sent for online hearings$")
    public void send_request_online_hearing(String type) throws Exception {

        RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<String> response = null;

        String endpoint = getEndpoints().get("online hearing");
        try {
            String json = JsonUtils.toJson(testContext.getScenarioContext().getCurrentOnlineHearingRequest());
            if ("GET".equalsIgnoreCase(type)) {
                response = restTemplate.getForEntity(baseUrl + endpoint, String.class);
                testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
                testContext.getScenarioContext()
                    .addCaseId(testContext.getScenarioContext().getCurrentOnlineHearingRequest().getCaseId());
            } else if ("POST".equalsIgnoreCase(type)) {
                HttpEntity<String> request = new HttpEntity<>(json, header);
                response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.POST, request, String.class);
                testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
                testContext.getScenarioContext()
                    .addCaseId(testContext.getScenarioContext().getCurrentOnlineHearingRequest().getCaseId());
            } else if ("PUT".equalsIgnoreCase(type)) {
                HttpEntity<String> request = new HttpEntity<>(getPutRequest(), header);
                String onlineHearingId = testContext.getScenarioContext().getCurrentOnlineHearing().getOnlineHearingId()
                    .toString();
                response = restTemplate
                    .exchange(baseUrl + endpoint + "/" + onlineHearingId, HttpMethod.PUT, request, String.class);
                testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
            }

        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setHttpResponseStatusCode(hcee.getRawStatusCode());
        }
    }

    @When("^a (.*) request is sent for a conversation")
    public void send_request(String type) throws Exception {

        RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<String> response = null;

        String endpoint = getEndpoints().get("conversations");
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        endpoint = endpoint.replaceAll("onlineHearing_id", String.valueOf(onlineHearing.getOnlineHearingId()));

        try {
            if ("GET".equalsIgnoreCase(type)) {
                HttpEntity<String> request = new HttpEntity<>("", header);
                response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.GET, request, String.class);
            } else {
                throw new PendingException("Only GET is supported for conversations");
            }
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setResponseBodyAndStatesForException(hcee);
        }
    }

    public String getPutRequest() throws Exception {
        return JsonUtils.toJson(testContext.getScenarioContext().getUpdateOnlineHearingRequest());
    }

    @And("^the request contains a random UUID$")
    public void the_request_contains_a_random_UUID() throws Exception {
        testContext.getScenarioContext().getCurrentOnlineHearing().setOnlineHearingId(UUID.randomUUID());
    }

    @Given("^a standard update online hearing request$")
    public void a_standard_update_online_hearing_request() throws IOException {
        UpdateOnlineHearingRequest request = JsonUtils
            .toObjectFromTestName("online_hearing/update_online_hearing", UpdateOnlineHearingRequest.class);
        testContext.getScenarioContext().setUpdateOnlineHearingRequest(request);
    }

    @And("^the update online hearing state is (.*)$")
    public void the_update_online_hearing_state_is(String stateName) {
        testContext.getScenarioContext().getUpdateOnlineHearingRequest().setState(stateName);
    }

    @And("^the relist reason is '(.*)'$")
    public void theRelistReasonIs(String reason) throws Exception {
        testContext.getScenarioContext().getUpdateOnlineHearingRequest().setReason(reason);
    }

    @Then("^the response contains (\\d) online hearings$")
    public void the_response_contains_no_online_hearings(int count) throws IOException {
        OnlineHearingsResponse response = JsonUtils
            .toObjectFromJson(testContext.getHttpContext().getRawResponseString(), OnlineHearingsResponse.class);
        assertEquals(count, response.getOnlineHearingResponses().size());
    }

    @Then("^the response contains online hearing with case '(.*)'$")
    public void the_response_contains_online_hearing_with_case(String caseId) throws IOException {
        OnlineHearingsResponse response = JsonUtils
            .toObjectFromJson(testContext.getHttpContext().getRawResponseString(), OnlineHearingsResponse.class);
        assertTrue(response.getOnlineHearingResponses().stream().anyMatch(o -> caseId.equalsIgnoreCase(o.getCaseId())));
    }

    @Then("^the online hearing state is '(.*)'$")
    public void the_online_hearing_state_is(String state) throws Exception {
        assertEquals(state, getOnlineHearingResponse().getCurrentState().getName());
    }

    @And("^the conversation response contains an online hearing$")
    public void theResponseContainsAnOnlineHearing() throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertNotNull(response.getOnlineHearing());
    }

    @And("^the conversation response contains an online hearing with state desc of '(.*)'$")
    public void theConversationResponseContainsAnOnlineHearingWithStateDesc(String desc) throws Throwable {
        ConversationResponse response = getConversationResponse();
        String uri = getExpectedOnlineHearingUri(response.getOnlineHearing().getOnlineHearingId());
        assertEquals(desc, getConversationResponse().getOnlineHearing().getCurrentState().getStateDesc());
    }

    @And("^the conversation response contains an online hearing with the correct uri$")
    public void theConversationResponseContainsAnOnlineHearingWithAUri() throws Throwable {
        ConversationResponse response = getConversationResponse();
        String uri = getExpectedOnlineHearingUri(response.getOnlineHearing().getOnlineHearingId());
        assertTrue(getConversationResponse().getOnlineHearing().getUri().equals(uri));
    }

    @And("^the conversation response contains an online hearing with (\\d) history entries$")
    public void theResponseContainsAnOnlineHearingWithHistory(int count) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertEquals(count, response.getOnlineHearing().getHistories().size());
    }

    @And("^the conversation response contains an online hearing with 1 history entry  with state desc of '(.*)'$")
    public void theResponseContainsAnOnlineHearingWithHistory(String stateName) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertTrue(response.getOnlineHearing().getHistories().stream().anyMatch(h -> h.getStateDesc().equals(stateName)));
    }

    @And("^the conversation response contains a decision$")
    public void theResponseContainsADecision() throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertNotNull(response.getOnlineHearing().getDecisionResponse());
    }

    @And("^the conversation response contains no decision$")
    public void theResponseContainsNoDecision() throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertNull(response.getOnlineHearing().getDecisionResponse());
    }

    @And("^the conversation response contains a decision with the correct uri$")
    public void theConversationResponseContainsADecisionWithAUri() throws Throwable {
        ConversationResponse response = getConversationResponse();
        String uri = getExpectedDecisionUri(response.getOnlineHearing().getOnlineHearingId());
        assertEquals(uri, getConversationResponse().getOnlineHearing().getDecisionResponse().getUri());
    }

    @And("^the conversation response contains a decision with state desc of '(.*)'$")
    public void theConversationResponseContainsADecisionWithStateDesc(String stateDesc) throws Throwable {
        ConversationResponse response = getConversationResponse();
        String uri = getExpectedDecisionUri(response.getOnlineHearing().getOnlineHearingId());
        assertEquals(stateDesc, getConversationResponse().getOnlineHearing().getDecisionResponse().getDecisionState().getStateDesc());
    }

    @And("^the conversation response contains a decision with 1 history entry with state desc of '(.*)'$")
    public void theResponseContainsADecisionWithHistory(String stateDesc) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertTrue(response.getOnlineHearing().getDecisionResponse().getHistories().stream().anyMatch(h -> h.getStateDesc().equals(stateDesc)));
    }

    @And("^the conversation response contains a decision with (\\d) history entries$")
    public void theResponseContainsADecisionWithHistory(int count) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertEquals(count, response.getOnlineHearing().getDecisionResponse().getHistories().size());
    }

    @And("^the conversation response contains (\\d+) decision replies$")
    public void theConversationResponseContainsDecisionReplies(int count) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertEquals(count, response.getOnlineHearing().getDecisionResponse().getDecisionReplyResponses().size());
    }

    @And("^the conversation response contains a decision reply with the correct uri$")
    public void theConversationResponseContainsADecisionReplyWithTheCorrectUri() throws Throwable {
        ConversationResponse response = getConversationResponse();
        String uri = getExpectedDecisionReplyUri(response.getOnlineHearing().getOnlineHearingId(),
            UUID.fromString(getDecisionReplyFromConversationResponse(0).getDecisionReplyId())
        );
        assertEquals(uri, getDecisionReplyFromConversationResponse(0).getUri());
    }

    @And("^the conversation response contains (\\d) question$")
    public void theResponseContainsAQuestion(int count) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertNotNull(response.getOnlineHearing().getQuestions());
        assertEquals(count, response.getOnlineHearing().getQuestions().size());
    }

    @And("^the conversation response contains a question with the correct uri$")
    public void theConversationResponseContainsAQuestionWithAUri() throws Throwable {
        ConversationResponse response = getConversationResponse();
        String uri = getExpectedQuestionUri(response.getOnlineHearing().getOnlineHearingId(),
            UUID.fromString(getQuestionFromConversationResponse(0).getQuestionId()));
        assertEquals(uri, getConversationResponse().getOnlineHearing().getQuestions().get(0).getUri());
    }

    @And("^the conversation response contains a question with state desc of '(.*)'$")
    public void theConversationResponseContainsAQuestionWithAStateDesc(String stateDesc) throws Throwable {
        ConversationResponse response = getConversationResponse();
        String uri = getExpectedQuestionUri(response.getOnlineHearing().getOnlineHearingId(), UUID.fromString(getQuestionFromConversationResponse(0).getQuestionId()));
        assertEquals(stateDesc, getConversationResponse().getOnlineHearing().getQuestions().get(0).getCurrentState().getStateDesc());
    }

    @And("^the conversation response contains a question with 1 history entry with state desc of '(.*)'$")
    public void theResponseContainsAQuestionWithHistory(String stateName) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertTrue(getQuestionFromConversationResponse(0).getHistories().stream().anyMatch(h -> h.getStateDesc().equals(stateName)));
    }

    @And("^the conversation response contains a question with (\\d) history entries$")
    public void theResponseContainsAQuestionWithHistory(int count) throws Throwable {
        assertEquals(count, getQuestionFromConversationResponse(0).getHistories().size());
    }

    @And("^the conversation response contains (\\d) answer$")
    public void theResponseContainsAnAnswer(int count) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertNotNull(response.getOnlineHearing().getQuestions());
        assertEquals(count, response.getOnlineHearing().getQuestions().get(0).getAnswers().size());
    }

    @And("^the conversation response contains an answer with the correct uri$")
    public void theConversationResponseContainsAnAnswerWithAUri() throws Throwable {
        ConversationResponse response = getConversationResponse();
        String uri = getExpectedAnswerUri(response.getOnlineHearing().getOnlineHearingId(),
            UUID.fromString(getQuestionFromConversationResponse(0).getQuestionId()),
            UUID.fromString(getAnswerFromConversationResponse(0).getAnswerId())
        );
        assertEquals(uri, getAnswerFromConversationResponse(0).getUri());
    }

    @And("^the conversation response contains an answer with state desc of '(.*)'$")
    public void theConversationResponseContainsAnAnswerWithAStateDesc(String stateDesc) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertEquals(stateDesc, getAnswerFromConversationResponse(0).getStateResponse().getStateDesc());
    }

    @And("^the conversation response contains an answer with (\\d) history entries$")
    public void theResponseContainsAnAnswerWithHistory(int count) throws Throwable {
        assertEquals(count, getQuestionFromConversationResponse(0).getAnswers().get(0).getHistories().size());
    }

    @And("^the conversation response contains an answer with 1 history entry with state desc of '(.*)'$")
    public void theResponseContainsAnAnswerWithHistoryStateDesc(String stateDesc) throws Throwable {
        assertEquals(stateDesc, getQuestionFromConversationResponse(0).getAnswers().get(0).getHistories().get(0).getStateDesc());
    }

    @And("^the panel member name is '(.*)'$")
    public void thePanelMemberNameIs(String name) throws Throwable {

        assertEquals(name, getOnlineHearingResponse().getPanel().get(0).getName());
    }

    @And("^the panel member role is '(.*)'$")
    public void thePanelMemberRoleIs(String role) throws Throwable {

        assertEquals(role, getOnlineHearingResponse().getPanel().get(0).getRole());
    }

    @And("^the online hearing end date is not null$")
    public void theOnlineHearingExpiryDateIsNotNull() throws Throwable {
        assertNotNull(getOnlineHearingResponse().getEndDate());
    }

    @And("^the online hearing reason is '(.*)'$")
    public void theOnlineHearingReasonIsReason(String reason) throws Throwable {
        assertEquals(reason, getOnlineHearingResponse().getRelistReason());
    }

    private ConversationResponse getConversationResponse() throws IOException {
        return JsonUtils
            .toObjectFromJson(testContext.getHttpContext().getRawResponseString(), ConversationResponse.class);
    }

    private QuestionResponse getQuestionFromConversationResponse(int index) throws IOException {
        ConversationResponse response = getConversationResponse();

        return response.getOnlineHearing().getQuestions().get(index);
    }

    private AnswerResponse getAnswerFromConversationResponse(int index) throws IOException {
        ConversationResponse response = getConversationResponse();

        return response.getOnlineHearing().getQuestions().get(index).getAnswers().get(index);
    }

    private DecisionReplyResponse getDecisionReplyFromConversationResponse(int index) throws IOException {
        ConversationResponse response = getConversationResponse();

        return response.getOnlineHearing().getDecisionResponse().getDecisionReplyResponses().get(index);
    }

    private String getExpectedOnlineHearingUri(UUID onlineHearingId) {
        return CohUriBuilder.buildOnlineHearingGet(onlineHearingId);
    }

    private String getExpectedDecisionUri(UUID decisionId) {
        return CohUriBuilder.buildDecisionGet(decisionId);
    }

    private String getExpectedQuestionUri(UUID onlineHearingId, UUID questionId) {
        return CohUriBuilder.buildQuestionGet(onlineHearingId, questionId);
    }

    private String getExpectedAnswerUri(UUID onlineHearingId, UUID questionId, UUID answerId) {
        return CohUriBuilder.buildAnswerGet(onlineHearingId, questionId, answerId);
    }

    private OnlineHearingResponse getOnlineHearingResponse() throws Exception {
        String rawResponseString = testContext.getHttpContext().getRawResponseString();
        return JsonUtils.toObjectFromJson(rawResponseString, OnlineHearingResponse.class);
    }

    private String getExpectedDecisionReplyUri(UUID onlineHearingId, UUID decisionReplyId) {
        return CohUriBuilder.buildDecisionReplyGet(onlineHearingId, decisionReplyId);
    }
}