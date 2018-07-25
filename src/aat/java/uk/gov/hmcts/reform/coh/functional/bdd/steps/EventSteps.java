package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.controller.events.EventRegistrationRequest;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;

import java.io.IOException;

public class EventSteps extends BaseSteps{

    private ResponseEntity<String> response;
    private String endpoint = "/continuous-online-hearings/events/register";
    @Autowired
    public EventSteps(TestContext testContext) {
        super(testContext);
        restTemplate = new RestTemplate();
    }

    @Given("^a standard request to subscribe to question round issued$")
    public void aStandardRequestToSubscribeToQuestionRoundIssued() throws IOException {
        String json = JsonUtils.getJsonInput("event_forwarding_register/subscribe_to_qr_issued");
        EventRegistrationRequest eventRegistrationRequest = (EventRegistrationRequest) JsonUtils.toObjectFromJson(json, EventRegistrationRequest.class);
        testContext.getScenarioContext().setEventRegistrationRequest(eventRegistrationRequest);
    }

    @When("^a POST request is sent to register$")
    public void aPostRequestIsSentToRegister() throws IOException {
        String json = JsonUtils.toJson(testContext.getScenarioContext().getEventRegistrationRequest());
        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(json, header);
        response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.POST, request, String.class);
        testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        testContext.getHttpContext().setHttpResponseStatusCode(response.getStatusCodeValue());
    }
}
