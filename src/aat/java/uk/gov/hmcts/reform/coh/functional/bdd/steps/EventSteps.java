package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
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
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.repository.*;
import uk.gov.hmcts.reform.coh.schedule.notifiers.EventNotifierJob;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;

public class EventSteps extends BaseSteps{

    @Autowired
    private SessionEventForwardingRegisterRepository sessionEventForwardingRegisterRepository;

    @Autowired
    private SessionEventRepository sessionEventRepository;

    @Autowired
    private JurisdictionRepository jurisdictionRepository;

    @Autowired
    private SessionEventTypeRespository sessionEventTypeRespository;

    @Autowired
    private EventNotifierJob job;


    private ResponseEntity<String> response;
    private String endpoint = "/continuous-online-hearings/events/register";

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

    @When("^the notification scheduler runs$")
    public void theNotificationSchedulerRuns() {
        job.execute();
    }

    @When("^the notification scheduler fails to send after configured retries for '(.*)' and event type '(.*)'$")
    public void theNotificationSchedulerFailsToSendAfterConfiguredRetries(String jurisdiction, String eventType) {
        // This is a bit crappy until we set up some kind of test jurisdiction
        Optional<Jurisdiction> optionalJurisdiction = jurisdictionRepository.findByJurisdictionName(jurisdiction);
        Optional<SessionEventType> sessionEventType = sessionEventTypeRespository.findByEventTypeName(eventType);

        Optional<SessionEventForwardingRegister> optSessionEventForwardingRegister = sessionEventForwardingRegisterRepository.findByJurisdictionAndSessionEventType(optionalJurisdiction.get(), sessionEventType.get());

        SessionEventForwardingRegister register = optSessionEventForwardingRegister.get();
        String originalEndpoint = register.getForwardingEndpoint();
        register.setForwardingEndpoint("https://0.0.0.0/nowhere");
        sessionEventForwardingRegisterRepository.save(register);
        for (int i = 0; i < register.getMaximumRetries()+2; i++) {
            job.execute();
        }
        register.setForwardingEndpoint(originalEndpoint);
        sessionEventForwardingRegisterRepository.save(register);
    }

    @Then("^the event status is (.*)$")
    public void theEventIsSent(String status) {
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        Iterable<SessionEvent> events = sessionEventRepository.findAll();
        StreamSupport.stream(events.spliterator(), false)
                .filter(e -> e.getOnlineHearing().getOnlineHearingId().equals(onlineHearing.getOnlineHearingId()))
                .forEach(e -> {
                    assertEquals(
                            status,
                            e.getSessionEventForwardingState().getForwardingStateName()
                    );
                }
                );
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
