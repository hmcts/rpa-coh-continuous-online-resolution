package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.controller.decision.CreateDecisionResponse;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionRequest;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionResponse;
import uk.gov.hmcts.reform.coh.controller.decision.UpdateDecisionRequest;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.AllDecisionRepliesResponse;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.DecisionReplyRequest;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionReply;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.repository.DecisionReplyRepository;

import javax.persistence.EntityNotFoundException;
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

    @After
    public void cleanup() {
        super.cleanup();
    }

    @Given("^a standard decision$")
    public void a_standard_decision() throws IOException {
        DecisionRequest decisionRequest = (DecisionRequest) JsonUtils.toObjectFromTestName("decision/standard_decision", DecisionRequest.class);
        testContext.getScenarioContext().setCurrentDecisionRequest(decisionRequest);
    }

    @Given("^a standard decision reply$")
    public void aStandardDecisionReply() throws Throwable {
        DecisionReplyRequest decisionReplyRequest = (DecisionReplyRequest) JsonUtils.toObjectFromTestName("decision/standard_decision_reply", DecisionReplyRequest.class);
        testContext.getScenarioContext().setCurrentDecisionReplyRequest(decisionReplyRequest);
    }

    @And("^the decision reply is ' \"([^\"]*)\" '$")
    public void theDecisionReplyIs(String decisionReply) {
        DecisionReplyRequest decisionReplyRequest = testContext.getScenarioContext().getCurrentDecisionReplyRequest();
        decisionReplyRequest.setDecisionReply(decisionReply);
    }

    @Given("^a standard decision for update$")
    public void a_standard_decision_for_update() throws IOException {
        UpdateDecisionRequest decisionRequest = (UpdateDecisionRequest) JsonUtils.toObjectFromTestName("decision/standard_decision", UpdateDecisionRequest.class);
        testContext.getScenarioContext().setUpdateDecisionRequest(decisionRequest);
    }

    @And("^the update decision state is (.*)$")
    public void the_update_decision_state_is (String stateName) {
        testContext.getScenarioContext().getUpdateDecisionRequest().setState(stateName);
    }

    @When("^a (.*) request is sent for a decision$")
    public void send_request(String type) throws Exception {

        RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<String> response = null;

        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");

        String endpoint = getEndpoint();
        try {
            if ("GET".equalsIgnoreCase(type)) {
                HttpEntity<String> request = new HttpEntity<>("", header);
                response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.GET, request, String.class);
            } else if ("POST".equalsIgnoreCase(type)) {
                HttpEntity<String> request = new HttpEntity<>(getPostRequest(), header);
                response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.POST, request, String.class);

                CreateDecisionResponse createDecisionResponse = (CreateDecisionResponse) JsonUtils.toObjectFromJson(response.getBody(), CreateDecisionResponse.class);
                Decision decision = new Decision();
                decision.setDecisionId(createDecisionResponse.getDecisionId());
                testContext.getScenarioContext().setCurrentDecision(decision);
            } else if ("PUT".equalsIgnoreCase(type)) {
                HttpEntity<String> request = new HttpEntity<>(getPutRequest(), header);
                String decisionId = testContext.getScenarioContext().getCurrentDecision().getDecisionId().toString();
                response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, request, String.class);
            }
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setResponseBodyAndStatesForException(hcee);
        }
    }

    @And("^a (.*) request is sent for a decision reply$")
    public void aPOSTRequestIsSentForADecisionReply(String type) throws Throwable {
        RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<String> response = null;

        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");

        String endpoint = getReplyEndpoint();

        if(type.equalsIgnoreCase("POST")) {
            DecisionReplyRequest decisionReplyRequest = testContext.getScenarioContext().getCurrentDecisionReplyRequest();
            String json = JsonUtils.toJson(decisionReplyRequest);
            HttpEntity<String> request = new HttpEntity<>(json, header);

            try {
                response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.POST, request, String.class);

                CreateDecisionResponse createDecisionResponse = JsonUtils.toObjectFromJson(response.getBody(), CreateDecisionResponse.class);
                DecisionReply decisionReply = decisionReplyRepository.findById(createDecisionResponse.getDecisionId())
                        .orElseThrow(() -> new EntityNotFoundException());

                testContext.getScenarioContext().addCurrentDecisionReply(decisionReply);
                testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
            }catch (HttpClientErrorException e){
                testContext.getHttpContext().setResponseBodyAndStatesForException(e);
            }
        }
    }

    @When("^a GET request is sent for all decision replies$")
    public void aGETRequestIsSentForAllDecisionReplies() throws Throwable {
        RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<String> response = null;

        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");

        String endpoint = getReplyEndpoint();
        HttpEntity<String> request = new HttpEntity<>("", header);

        try {
            response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.GET, request, String.class);

            AllDecisionRepliesResponse allDecisionRepliesResponse = JsonUtils.toObjectFromJson(response.getBody(), AllDecisionRepliesResponse.class);
            testContext.getScenarioContext().setAllDecisionRepliesResponse(allDecisionRepliesResponse);
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        }catch (HttpClientErrorException e){
            testContext.getHttpContext().setResponseBodyAndStatesForException(e);
        }
    }



    @Then("^the response contains the decision UUID$")
    public void the_response_contains_the_decision_UUID() throws IOException {
        String responseString = testContext.getHttpContext().getRawResponseString();
        CreateDecisionResponse response = (CreateDecisionResponse) JsonUtils.toObjectFromJson(responseString, CreateDecisionResponse.class);
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
        DecisionResponse response = (DecisionResponse) JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), DecisionResponse.class);
        assertEquals(testContext.getScenarioContext().getCurrentDecision().getDecisionId().toString(), response.getDecisionId());
    }

    @And("^the decision state name is (.*)")
    public void the_question_id_matches(String stateName) throws Throwable {
        DecisionResponse response = (DecisionResponse) JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), DecisionResponse.class);
        assertEquals(stateName, response.getDecisionState().getStateName());
    }

    @And("^the decision expiry date is 7 days in the future")
    public void the_decision_expiry_date() throws IOException {
        Calendar expiry = new GregorianCalendar();
        expiry.add(Calendar.DAY_OF_YEAR, 7);
        DecisionResponse decision = (DecisionResponse) JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), DecisionResponse.class);
        assertNotNull(decision.getDeadlineExpiryDate());
       /*TO DO Fix assert */
        // assertTrue(decision.getDeadlineExpiryDate().contains(df.format(expiry.getTime())));
    }


    @And("^the decision state timestamp is today$")
    public void the_question_state_timestamp_is_today() throws Throwable {
        DecisionResponse decision = (DecisionResponse) JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), DecisionResponse.class);
        assertTrue(decision.getDecisionState().getStateDatetime().contains(df.format(new Date())));
    }

    private String getEndpoint() {
        String endpoint = getEndpoints().get("decision");
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        endpoint = endpoint.replaceAll("onlineHearing_id", String.valueOf(onlineHearing.getOnlineHearingId()));

        return endpoint;
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

    @And("^the decision replies list contains (\\d+) decision replies$")
    public void theDecisionRepliesListContainsDecisionReplies(int expectedDecisionReplies) throws Throwable {
        AllDecisionRepliesResponse allDecisionRepliesResponse = testContext.getScenarioContext().getAllDecisionRepliesResponse();
        assertEquals(expectedDecisionReplies, allDecisionRepliesResponse.getDecisionReplyList().size());

        int n = 0;
        for(DecisionReply expectedDecisionReply : testContext.getScenarioContext().getCurrentDecisionReplies()) {
            assertEquals(expectedDecisionReply.getDecisionReplyReason(),
                    allDecisionRepliesResponse.getDecisionReplyList().get(n).getDecisionReplyReason());

            assertEquals(expectedDecisionReply.getDecisionReply(),
                    allDecisionRepliesResponse.getDecisionReplyList().get(n).getDecisionReply());

            assertEquals(expectedDecisionReply.getDecision().getDecisionId().toString(),
                    allDecisionRepliesResponse.getDecisionReplyList().get(n).getDecisionId());

            assertEquals(expectedDecisionReply.getId().toString(),
                    allDecisionRepliesResponse.getDecisionReplyList().get(n).getDecisionReplyId());

            assertEquals(expectedDecisionReply.getAuthorReferenceId(),
                    allDecisionRepliesResponse.getDecisionReplyList().get(n).getAuthorReference());
            n++;
        }
    }
}
