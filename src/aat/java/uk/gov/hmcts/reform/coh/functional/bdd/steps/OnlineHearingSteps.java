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
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingRequest;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingsResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.UpdateOnlineHearingRequest;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;

import java.io.IOException;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class OnlineHearingSteps extends BaseSteps {

    private UpdateOnlineHearingRequest request;

    @Autowired
    public OnlineHearingSteps(TestContext testContext){
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

    @Given("^a standard online hearing$")
    public void a_standard_online_hearing() throws IOException {
        OnlineHearingRequest onlineHearingRequest = (OnlineHearingRequest) JsonUtils.toObjectFromTestName("online_hearing/standard_online_hearing", OnlineHearingRequest.class);
        testContext.getScenarioContext().setCurrentOnlineHearingRequest(onlineHearingRequest);
    }

    @Given("^the case id is '(.*)'")
    public void the_case_id_is(String caseId) {
        testContext.getScenarioContext().getCurrentOnlineHearingRequest().setCaseId(caseId);
    }

    @When("^a (.*) request is sent for online hearings$")
    public void send_request(String type) throws Exception {

        RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<String> response = null;

        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");

        String endpoint = getEndpoints().get("online hearing");
        try {
            String json = JsonUtils.toJson(testContext.getScenarioContext().getCurrentOnlineHearingRequest());
            if ("GET".equalsIgnoreCase(type)) {
                response = restTemplate.getForEntity(baseUrl + endpoint, String.class);
                testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
                testContext.getScenarioContext().addCaseId(testContext.getScenarioContext().getCurrentOnlineHearingRequest().getCaseId());
            } else if ("POST".equalsIgnoreCase(type)) {
                HttpEntity<String> request = new HttpEntity<>(json, header);
                response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.POST, request, String.class);
                testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
                testContext.getScenarioContext().addCaseId(testContext.getScenarioContext().getCurrentOnlineHearingRequest().getCaseId());
            } else if ("PUT".equalsIgnoreCase(type)) {
                HttpEntity<String> request = new HttpEntity<>(getPutRequest(), header);
                String onlineHearingId = testContext.getScenarioContext().getCurrentOnlineHearing().getOnlineHearingId().toString();
                response = restTemplate.exchange(baseUrl + endpoint + "/" + onlineHearingId, HttpMethod.PUT, request, String.class);
                testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
            }

        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setHttpResponseStatusCode(hcee.getRawStatusCode());
        }
    }

    public String getPutRequest() throws Exception {
        return  JsonUtils.toJson(testContext.getScenarioContext().getUpdateOnlineHearingRequest());
    }

    @And("^the request contains a random UUID$")
    public void the_request_contains_a_random_UUID() throws Exception {
        testContext.getScenarioContext().getCurrentOnlineHearing().setOnlineHearingId(UUID.randomUUID());
    }

    @Given("^a standard update online hearing request$")
    public void a_standard_update_online_hearing_request() throws IOException {
        UpdateOnlineHearingRequest request = (UpdateOnlineHearingRequest) JsonUtils.toObjectFromTestName("online_hearing/update_online_hearing", UpdateOnlineHearingRequest.class);
        testContext.getScenarioContext().setUpdateOnlineHearingRequest(request);
    }

    @And("^the update online hearing state is (.*)$")
    public void the_update_online_hearing_state_is (String stateName) {
        testContext.getScenarioContext().getUpdateOnlineHearingRequest().setState(stateName);
    }

    @Then("^the response contains (\\d) online hearings$")
    public void the_response_contains_no_online_hearings(int count) throws IOException {
        OnlineHearingsResponse response = (OnlineHearingsResponse) JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), OnlineHearingsResponse.class);
        assertEquals(count, response.getOnlineHearingResponses().size());
    }

    @Then("^the response contains online hearing with case '(.*)'$")
    public void the_response_contains_online_hearing_with_case(String caseId) throws IOException {
        OnlineHearingsResponse response = (OnlineHearingsResponse) JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), OnlineHearingsResponse.class);
        assertTrue(response.getOnlineHearingResponses().stream().anyMatch(o -> caseId.equalsIgnoreCase(o.getCaseId())));
    }
}
