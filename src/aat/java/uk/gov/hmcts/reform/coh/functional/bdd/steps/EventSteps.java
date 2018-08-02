package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.coh.controller.events.EventRegistrationRequest;
import uk.gov.hmcts.reform.coh.controller.events.ResetSessionEventRequest;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;

public class EventSteps extends BaseSteps{

    private ResponseEntity<String> response;
    private String endpoint = "/continuous-online-hearings/events";

    @Autowired
    private JurisdictionRepository jurisdictionRepository;

    @Autowired
    public EventSteps(TestContext testContext) {
        super(testContext);
        restTemplate = getRestTemplate();
    }

    @Before
    public void setup() throws Exception {
        super.setup();
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
        response = restTemplate.exchange(baseUrl + endpoint  + "/register", HttpMethod.POST, request, String.class);
        testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        testContext.getHttpContext().setHttpResponseStatusCode(response.getStatusCodeValue());
    }

    @When("^the put request is sent to reset the events of type (.*)$")
    public void thePutRequestIsSentToResetTheEventsOfTypeAnswers_submitted(String eventType) throws IOException {
        String json = JsonUtils.getJsonInput("event_forwarding_register/reset_answer_submitted_events");
        ResetSessionEventRequest resetSessionEventRequest = (ResetSessionEventRequest) JsonUtils.toObjectFromJson(json, ResetSessionEventRequest.class);

        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        Jurisdiction jurisdiction = jurisdictionRepository.findByJurisdictionName(onlineHearing.getJurisdiction().getJurisdictionName())
                .orElseThrow(() -> new EntityNotFoundException());

        resetSessionEventRequest.setJurisdiction(jurisdiction.getJurisdictionName());
        resetSessionEventRequest.setEventType(eventType);
        json = JsonUtils.toJson(resetSessionEventRequest);

        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(json, header);
        try {
            response = restTemplate.exchange(baseUrl + endpoint + "/reset", HttpMethod.PUT, request, String.class);
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
            testContext.getHttpContext().setHttpResponseStatusCode(response.getStatusCodeValue());
        }catch (HttpClientErrorException e) {
            testContext.getHttpContext().setResponseBodyAndStatesForException(e);
        }
    }
}
