package uk.gov.hmcts.reform.coh.functional.bdd.steps;

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
import uk.gov.hmcts.reform.coh.controller.decision.CreateDecisionResponse;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionRequest;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionResponse;
import uk.gov.hmcts.reform.coh.controller.decision.UpdateDecisionRequest;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.AllDecisionRepliesResponse;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.CreateDecisionReplyResponse;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.DecisionReplyRequest;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.DecisionReplyResponse;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionReply;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohEntityTypes;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.repository.DecisionReplyRepository;
import uk.gov.hmcts.reform.coh.states.DecisionsStates;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import javax.persistence.EntityNotFoundException;
import javax.transaction.NotSupportedException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DecisionSteps extends BaseSteps {

    @Autowired
    private DecisionReplyRepository decisionReplyRepository;

    @Autowired
    public DecisionSteps(TestContext testContext){
        super(testContext);
    }

    @Before
    public void setup() throws Exception {
        super.setup();
    }

    @Given("^a standard decision$")
    public void a_standard_decision() throws IOException {
        DecisionRequest decisionRequest = JsonUtils.toObjectFromTestName("decision/standard_decision", DecisionRequest.class);
        testContext.getScenarioContext().setCurrentDecisionRequest(decisionRequest);
    }

    @Given("^a standard decision reply$")
    public void aStandardDecisionReply() throws Throwable {
        DecisionReplyRequest decisionReplyRequest = JsonUtils.toObjectFromTestName("decision/standard_decision_reply", DecisionReplyRequest.class);
        testContext.getScenarioContext().setCurrentDecisionReplyRequest(decisionReplyRequest);
    }

    @And("^the decision reply is ' \"([^\"]*)\" '$")
    public void theDecisionReplyIs(String decisionReply) {
        DecisionReplyRequest decisionReplyRequest = testContext.getScenarioContext().getCurrentDecisionReplyRequest();
        decisionReplyRequest.setDecisionReply(decisionReply);
    }

    @Given("^a standard decision for update$")
    public void a_standard_decision_for_update() throws IOException {
        UpdateDecisionRequest decisionRequest = JsonUtils.toObjectFromTestName("decision/standard_decision", UpdateDecisionRequest.class);
        testContext.getScenarioContext().setUpdateDecisionRequest(decisionRequest);
    }

    @And("^the update decision state is (.*)$")
    public void the_update_decision_state_is (String stateName) {
        testContext.getScenarioContext().getUpdateDecisionRequest().setState(stateName);
    }

    @When("^a (.*) request is sent for a decision$")
    public void sendDecisionRequest(String method) throws Exception {

        testContext.getScenarioContext().clearDecisionReplies();
        try {
            ResponseEntity response = sendRequest(CohEntityTypes.DECISION, method, getDecisionRequest(method));
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);

            if ("POST".equalsIgnoreCase(method)) {
                CreateDecisionResponse createDecisionResponse = JsonUtils.toObjectFromJson(response.getBody().toString(), CreateDecisionResponse.class);
                Decision decision = new Decision();
                decision.setDecisionId(createDecisionResponse.getDecisionId());
                testContext.getScenarioContext().setCurrentDecision(decision);
            }
        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(hcee);
        }
    }

    @And("^a (.*) request is sent for a decision reply$")
    public void aPOSTRequestIsSentForADecisionReply(String type) throws Throwable {
        RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<String> response = null;

        String endpoint = getReplyEndpoint();

        if (type.equalsIgnoreCase("POST")) {
            DecisionReplyRequest decisionReplyRequest = testContext.getScenarioContext().getCurrentDecisionReplyRequest();
            String json = JsonUtils.toJson(decisionReplyRequest);
            HttpEntity<String> request = new HttpEntity<>(json, header);

            try {
                response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.POST, request, String.class);
                CreateDecisionReplyResponse createDecisionReplyResponse = JsonUtils.toObjectFromJson(response.getBody(), CreateDecisionReplyResponse.class);

                DecisionReply decisionReply = decisionReplyRepository.findById(createDecisionReplyResponse.getDecisionReplyId())
                        .orElseThrow(() -> new EntityNotFoundException());

                testContext.getScenarioContext().addDecisionReply(decisionReply);
                testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
            } catch (HttpClientErrorException e){
                testContext.getHttpContext().setResponseBodyAndStatesForResponse(e);
            }
        } else if(type.equalsIgnoreCase("GET")) {
            HttpEntity<String> request = new HttpEntity<>("", header);

            try {
                String uuid = testContext.getScenarioContext().getDecisionReplies().get(0).getId().toString();
                response = restTemplate.exchange(baseUrl + endpoint + "/" + uuid, HttpMethod.GET, request, String.class);
                DecisionReplyResponse decisionReplyResponse = JsonUtils.toObjectFromJson(response.getBody(), DecisionReplyResponse.class);
                testContext.getScenarioContext().setDecisionReplyResponse(decisionReplyResponse);
                testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
            } catch (HttpClientErrorException e) {
                testContext.getHttpContext().setResponseBodyAndStatesForResponse(e);
            }
        } else {
            throw new NotSupportedException("Method not support: " + type);
        }
    }

    @When("^a GET request is sent for all decision replies$")
    public void aGETRequestIsSentForAllDecisionReplies() throws Throwable {
        RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<String> response = null;

        String endpoint = getReplyEndpoint();
        HttpEntity<String> request = new HttpEntity<>("", header);

        try {
            response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.GET, request, String.class);

            AllDecisionRepliesResponse allDecisionRepliesResponse = JsonUtils.toObjectFromJson(response.getBody(), AllDecisionRepliesResponse.class);
            testContext.getScenarioContext().setAllDecisionRepliesResponse(allDecisionRepliesResponse);
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        } catch (HttpClientErrorException e) {
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(e);
        }
    }

    @Then("^the response contains the decision UUID$")
    public void the_response_contains_the_decision_UUID() throws IOException {
        String responseString = testContext.getHttpContext().getRawResponseString();
        CreateDecisionResponse response = JsonUtils.toObjectFromJson(responseString, CreateDecisionResponse.class);
        assertNotNull(response.getDecisionId());
    }

    public String getPostRequest() throws Exception {
        return  JsonUtils.toJson(testContext.getScenarioContext().getCurrentDecisionRequest());
    }

    public String getPutRequest() throws Exception {
        return  JsonUtils.toJson(testContext.getScenarioContext().getUpdateDecisionRequest());
    }

    @And("^the decision id matches$")
    public void the_question_id_matches() throws Throwable {
        DecisionResponse response = JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), DecisionResponse.class);
        assertEquals(testContext.getScenarioContext().getCurrentDecision().getDecisionId().toString(), response.getDecisionId());
    }

    @And("^the decision state name is (.*)")
    public void the_question_id_matches(String stateName) throws Throwable {
        DecisionResponse response = JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), DecisionResponse.class);
        assertEquals(stateName, response.getDecisionState().getName());
    }

    @And("^the decision expiry date is 7 days in the future")
    public void the_decision_expiry_date() throws IOException {
        Calendar expiry = new GregorianCalendar();
        expiry.add(Calendar.DAY_OF_YEAR, 7);
        DecisionResponse decision = JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), DecisionResponse.class);
        assertNotNull(decision.getDeadlineExpiryDate());
       /*TO DO Fix assert */
        // assertTrue(decision.getDeadlineExpiryDate().contains(df.format(expiry.getTime())));
    }


    @And("^the decision state timestamp is today$")
    public void the_question_state_timestamp_is_today() throws Throwable {
        DecisionResponse decision = JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), DecisionResponse.class);
        assertTrue(decision.getDecisionState().getDatetime().contains(df.format(new Date())));
    }

    private String getReplyEndpoint() {
        String endpoint = getEndpoints().get("decisionreply");
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        endpoint = endpoint.replaceAll("onlineHearing_id", String.valueOf(onlineHearing.getOnlineHearingId()));

        return endpoint;
    }

    @And("^the decision expiry date empty$")
    public void theDecisionExpiryDateEmpty() throws Throwable {
        DecisionResponse decision = JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), DecisionResponse.class);
        assertNull(decision.getDeadlineExpiryDate());
    }

    @And("^the decision replies list contains (\\d+) decision repl(?:y|ies)$")
    public void theDecisionRepliesListContainsDecisionReplies(int expectedDecisionReplies) {
        AllDecisionRepliesResponse allDecisionRepliesResponse =
                testContext.getScenarioContext().getAllDecisionRepliesResponse();

        int n = 0;
        for (DecisionReply expectedDecisionReply : testContext.getScenarioContext().getDecisionReplies()) {
            DecisionReplyResponse decisionReplyResponse = allDecisionRepliesResponse.getDecisionReplyList().get(n);

            assertNotNull(decisionReplyResponse);

            assertEquals(expectedDecisionReply.getDecisionReplyReason(),
                    decisionReplyResponse.getDecisionReplyReason());

            String replyState =
                    expectedDecisionReply.getDecisionReply()
                            ? DecisionsStates.DECISIONS_ACCEPTED.getStateName()
                            : DecisionsStates.DECISIONS_REJECTED.getStateName();

            assertEquals(replyState, decisionReplyResponse.getDecisionReply());

            assertEquals(expectedDecisionReply.getDecision().getDecisionId().toString(),
                    decisionReplyResponse.getDecisionId());

            assertEquals(expectedDecisionReply.getId().toString(), decisionReplyResponse.getDecisionReplyId());

            assertEquals(expectedDecisionReply.getAuthorReferenceId(), decisionReplyResponse.getAuthorReference());

            n++;
        }

        assertEquals(expectedDecisionReplies, n);
    }

    @And("^the decision reply contains all the fields$")
    public void theDecisionReplyContainsAllTheFields() {
        DecisionReplyResponse decisionReplyResponse = testContext.getScenarioContext().getDecisionReplyResponse();
        DecisionReply expectedDecisionReply = testContext.getScenarioContext().getDecisionReplies().stream()
                                                .filter(dr -> dr.getId().toString().equalsIgnoreCase(decisionReplyResponse.getDecisionReplyId()))
                                                .findFirst()
                                                .orElseThrow(() -> new EntityNotFoundException());

        assertEquals(expectedDecisionReply.getDecision().getDecisionId().toString(), decisionReplyResponse.getDecisionId());
        assertEquals(expectedDecisionReply.getDecisionReplyReason(), decisionReplyResponse.getDecisionReplyReason());

        String replyState =
                expectedDecisionReply.getDecisionReply()
                        ? DecisionsStates.DECISIONS_ACCEPTED.getStateName()
                        : DecisionsStates.DECISIONS_REJECTED.getStateName();
        assertEquals(replyState, decisionReplyResponse.getDecisionReply());
        assertEquals(expectedDecisionReply.getAuthorReferenceId(), decisionReplyResponse.getAuthorReference());
    }

    private String getDecisionRequest(String method) throws Exception {
        if (HttpMethod.POST.name().equalsIgnoreCase(method)) {
            return getPostRequest();
        } else if (HttpMethod.PUT.name().equalsIgnoreCase(method)) {
            return getPutRequest();
        } else {
            return "";
        }
    }
}
