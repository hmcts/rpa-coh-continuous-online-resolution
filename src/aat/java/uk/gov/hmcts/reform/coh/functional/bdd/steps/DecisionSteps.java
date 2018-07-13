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
import uk.gov.hmcts.reform.coh.controller.question.QuestionResponse;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;

import java.io.IOException;
import java.util.Date;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;

public class DecisionSteps extends BaseSteps {

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

    @When("^a (.*) request is sent for a decision$")
    public void send_request(String type) throws Exception {

        RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<String> response = null;

        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");

        String endpoint = getEndpoint();
        try {
            String json = getPostRequest();
            if ("GET".equalsIgnoreCase(type)) {
                HttpEntity<String> request = new HttpEntity<>("", header);
                //response = restTemplate.getForEntity(baseUrl + endpoint, String.class);
                response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.GET, request, String.class);
            } else if ("POST".equalsIgnoreCase(type)) {
                HttpEntity<String> request = new HttpEntity<>(json, header);
                response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.POST, request, String.class);

                CreateDecisionResponse createDecisionResponse = (CreateDecisionResponse) JsonUtils.toObjectFromJson(response.getBody(), CreateDecisionResponse.class);
                Decision decision = new Decision();
                decision.setDecisionId(createDecisionResponse.getDecisionId());
                testContext.getScenarioContext().setCurrentDecision(decision);
            } else if ("PATCH".equalsIgnoreCase(type)) {
                /**
                 This is a workaround for https://jira.spring.io/browse/SPR-15347
                 **/
                HttpEntity<String> request = new HttpEntity<>(json, header);
                response = restTemplate.exchange(baseUrl + endpoint + "?_method=patch", HttpMethod.POST, request, String.class);
            }
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setHttpResponseStatusCode(hcee.getRawStatusCode());
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
}
