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
import uk.gov.hmcts.reform.coh.controller.events.EventRegistrationRequest;
import uk.gov.hmcts.reform.coh.controller.events.ResetSessionEventRequest;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingRegisterRepository;
import uk.gov.hmcts.reform.coh.repository.SessionEventRepository;
import uk.gov.hmcts.reform.coh.repository.SessionEventTypeRespository;
import uk.gov.hmcts.reform.coh.schedule.notifiers.EventNotifierJob;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;

public class EventSteps extends BaseSteps {

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
    private String endpoint = "/continuous-online-hearings/events";

    @Autowired
    public EventSteps(TestContext testContext) {
        super(testContext);
    }

    @Before
    public void setUp() throws Exception {
        super.setup();
    }

    @And("^jurisdiction ' \"([^\"]*)\", with id ' \"(\\d+)\" ' and max question rounds ' \"(\\d+)\" ' is created$")
    public void aJurisdictionNamedWithUrlAndMaxQuestionRoundsIsCreated(String jurisdictionName, Long id,  int maxQuestionRounds) {
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setJurisdictionId(id);
        jurisdiction.setJurisdictionName(jurisdictionName);
        jurisdiction.setMaxQuestionRounds(maxQuestionRounds);
        jurisdictionRepository.save(jurisdiction);
        testContext.getScenarioContext().getEventRegistrationRequest().setJurisdiction(jurisdiction.getJurisdictionName());
        testContext.getScenarioContext().addJurisdiction(jurisdiction);
    }

    @Given("^a conflicting request to subscribe to question round issued$")
    public void aConflictingRequestToSubscribeToQuestionRoundIssued() throws IOException {
        String json = JsonUtils.getJsonInput("event_forwarding_register/subscribe_to_qr_issued");
        EventRegistrationRequest eventRegistrationRequest = JsonUtils.toObjectFromJson(json, EventRegistrationRequest.class);
        testContext.getScenarioContext().setEventRegistrationRequest(eventRegistrationRequest);
    }

    @When("^a POST request is sent to register$")
    public void aPostRequestIsSentToRegister() throws IOException {
        String json = JsonUtils.toJson(testContext.getScenarioContext().getEventRegistrationRequest());
        HttpEntity<String> request = new HttpEntity<>(json, header);
        try {
            response = getRestTemplate().exchange(baseUrl + endpoint  + "/register", HttpMethod.POST, request, String.class);
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
            testContext.getHttpContext().setHttpResponseStatusCode(response.getStatusCodeValue());
        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setResponseBodyAndStatesForException(hcee);
        }
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
    public void thePutRequestIsSentToResetTheEventsOfTypeAnswersSubmitted(String eventType) throws IOException {
        String json = JsonUtils.getJsonInput("event_forwarding_register/reset_answer_submitted_events");
        ResetSessionEventRequest resetSessionEventRequest = JsonUtils.toObjectFromJson(json, ResetSessionEventRequest.class);

        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        Jurisdiction jurisdiction = jurisdictionRepository.findByJurisdictionName(onlineHearing.getJurisdiction().getJurisdictionName())
                .orElseThrow(() -> new EntityNotFoundException());

        resetSessionEventRequest.setJurisdiction(jurisdiction.getJurisdictionName());
        resetSessionEventRequest.setEventType(eventType);
        json = JsonUtils.toJson(resetSessionEventRequest);

        HttpEntity<String> request = new HttpEntity<>(json, header);
        try {
            response = getRestTemplate().exchange(baseUrl + endpoint + "/reset", HttpMethod.PUT, request, String.class);
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
            testContext.getHttpContext().setHttpResponseStatusCode(response.getStatusCodeValue());
        }catch (HttpClientErrorException e) {
            testContext.getHttpContext().setResponseBodyAndStatesForException(e);
        }
    }

    @And("^a standard event register request$")
    public void aStandardEventRegisterRequest() throws IOException{
        String json = JsonUtils.getJsonInput("event_forwarding_register/subscribe_to_qr_issued");
        EventRegistrationRequest eventRegistrationRequest = JsonUtils.toObjectFromJson(json, EventRegistrationRequest.class);
        testContext.getScenarioContext().setEventRegistrationRequest(eventRegistrationRequest);
    }

    @And("^an invalid '\"([^\"]*)\"'$")
    public void setInvalidProperty(String property) {

        if (property.equalsIgnoreCase("jurisdiction")){
            testContext.getScenarioContext().getEventRegistrationRequest().setJurisdiction("invalid");
        }
        if (property.equalsIgnoreCase("eventType")){
            testContext.getScenarioContext().getEventRegistrationRequest().setEventType("invalid");
        }
        if (property.equalsIgnoreCase("url")){
            testContext.getScenarioContext().getEventRegistrationRequest().setEndpoint("invalid");
        }

    }


}
