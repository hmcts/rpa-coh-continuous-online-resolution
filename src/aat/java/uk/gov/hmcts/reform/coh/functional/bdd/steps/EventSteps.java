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
import uk.gov.hmcts.reform.coh.controller.events.EventRegistrationRequest;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.SessionEvent;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.repository.*;
import uk.gov.hmcts.reform.coh.schedule.notifiers.EventNotifierJob;
import uk.gov.hmcts.reform.coh.states.SessionEventForwardingStates;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class EventSteps extends BaseSteps{

    @Autowired
    private SessionEventForwardingStateRepository sessionEventForwardingStateRepository;

    @Autowired
    private SessionEventForwardingRegisterRepository sessionEventForwardingRegisterRepository;

    @Autowired
    private SessionEventForwardingRegister sessionEventForwardingRegister;

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
        response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.POST, request, String.class);
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

        Optional<SessionEventForwardingRegister> sessionEventForwardingRegister = sessionEventForwardingRegisterRepository.findByJurisdictionAndSessionEventType(optionalJurisdiction.get(), sessionEventType.get());

        for (int i = 0; i < sessionEventForwardingRegister.get().getMaximumRetries(); i++) {
            job.execute();
        }
    }

    @Then("^the event is sent$")
    public void theEventIsSent() {
        Iterable<SessionEvent> events = sessionEventRepository.findAll();
        events.forEach(e -> {
            assertEquals(
                    SessionEventForwardingStates.EVENT_FORWARDING_SUCCESS.getStateName(),
                    e.getSessionEventForwardingState().getForwardingStateName()
            );
        }
        );
    }
}
