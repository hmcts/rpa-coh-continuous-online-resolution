package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.coh.controller.events.EventRegistrationRequest;
import uk.gov.hmcts.reform.coh.controller.events.SessionEventRequest;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohEntityTypes;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.idam.IdamAuthentication;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingRegisterRepository;
import uk.gov.hmcts.reform.coh.repository.SessionEventRepository;
import uk.gov.hmcts.reform.coh.repository.SessionEventTypeRespository;
import uk.gov.hmcts.reform.coh.schedule.notifiers.BasicJsonNotificationForwarder;
import uk.gov.hmcts.reform.coh.schedule.notifiers.EventNotifierJob;
import uk.gov.hmcts.reform.coh.schedule.trigger.EventTriggerJob;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import javax.persistence.EntityNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    private EventNotifierJob notifierJob;

    @Autowired
    private EventTriggerJob triggerJob;

    @Autowired
    private AuthTokenValidator authTokenValidator;

    @Value("${idam.s2s-auth.microservice}")
    private String expectedMicroserviceName;

    private ResponseEntity<String> response;

    @Autowired
    public EventSteps(TestContext testContext, IdamAuthentication idamAuthentication) {
        super(testContext, idamAuthentication);
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
        testContext.getScenarioContext().setCurrentJurisdiction(jurisdiction);
    }

    @Given("^a conflicting request to subscribe to question round issued$")
    public void aConflictingRequestToSubscribeToQuestionRoundIssued() throws IOException {
        String json = JsonUtils.getJsonInput("event_forwarding_register/subscribe_to_qr_issued");
        EventRegistrationRequest eventRegistrationRequest = JsonUtils.toObjectFromJson(json, EventRegistrationRequest.class);
        testContext.getScenarioContext().setEventRegistrationRequest(eventRegistrationRequest);
    }

    @When("^a (.*) request is sent to register$")
    public void aRequestIsSentToRegister(String method) throws IOException {

        String json = JsonUtils.toJson(testContext.getScenarioContext().getEventRegistrationRequest());
        try {
            ResponseEntity<String> response = sendRequest(CohEntityTypes.EVENT, HttpMethod.valueOf(method).toString(), json);
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(hcee);
        }
    }

    @When("^the notification scheduler runs$")
    public void theNotificationSchedulerRuns() {
        notifierJob.execute();
    }

    @When("^the trigger scheduler runs$")
    public void theTriggerSchedulerRuns() {
        triggerJob.execute();
    }

    @When("^the notification scheduler fails to send after configured retries for '(.*)' and event type '(.*)'$")
    public void theNotificationSchedulerFailsToSendAfterConfiguredRetries(String jurisdiction, String eventType) {
        // This is a bit crappy until we set up some kind of test jurisdiction
        Optional<Jurisdiction> optionalJurisdiction = jurisdictionRepository.findByJurisdictionName(jurisdiction);
        Optional<SessionEventType> sessionEventType = sessionEventTypeRespository.findByEventTypeName(eventType);

        Optional<SessionEventForwardingRegister> optSessionEventForwardingRegister = sessionEventForwardingRegisterRepository.findByJurisdictionAndSessionEventType(optionalJurisdiction.get(), sessionEventType.get());

        SessionEventForwardingRegister register = optSessionEventForwardingRegister.get();
        String originalEndpoint = register.getForwardingEndpoint();
        try {
            register.setForwardingEndpoint("https://0.0.0.0/nowhere");
            sessionEventForwardingRegisterRepository.save(register);
            for (int i = 0; i < register.getMaximumRetries() + 2; i++) {
                notifierJob.execute();
            }
        } finally {
            register.setForwardingEndpoint(originalEndpoint);
            sessionEventForwardingRegisterRepository.save(register);
        }
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
        SessionEventRequest resetSessionEventRequest = JsonUtils.toObjectFromJson(json, SessionEventRequest.class);

        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        Jurisdiction jurisdiction = jurisdictionRepository.findByJurisdictionName(onlineHearing.getJurisdiction().getJurisdictionName())
                .orElseThrow(() -> new EntityNotFoundException());

        resetSessionEventRequest.setJurisdiction(jurisdiction.getJurisdictionName());
        resetSessionEventRequest.setEventType(eventType);
        json = JsonUtils.toJson(resetSessionEventRequest);

        HttpEntity<String> request = new HttpEntity<>(json, header);
        try {
            response = getRestTemplate().exchange(baseUrl + CohUriBuilder.buildEventResetPut(), HttpMethod.PUT, request, String.class);
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        } catch (HttpClientErrorException e) {
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(e);
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

    @And("^the registration endpoint is '(.*)'$")
    public void theRegistrationEndpointIs(String endpoint) {
        testContext.getScenarioContext().getEventRegistrationRequest().setEndpoint(endpoint);
    }

    @Then("^the event register endpoint is '(.*)'$")
    public void theEventRegisterEndpointIs(String endpoint) {
        Jurisdiction jurisdiction = testContext.getScenarioContext().getCurrentJurisdiction();
        List<SessionEventForwardingRegister> register = sessionEventForwardingRegisterRepository.findByJurisdiction(jurisdiction);
        assertEquals(endpoint, register.get(0).getForwardingEndpoint());
    }

    @And("^the event register is saved")
    public void theEventRegisterIsSaved() {
        theEventRegisterCountIs(1);
    }

    @And("^the event register is deleted$")
    public void theEventRegisterIsDeleted() {
        theEventRegisterCountIs(0);
    }

    private void theEventRegisterCountIs(int count) {
        Jurisdiction jurisdiction = testContext.getScenarioContext().getCurrentJurisdiction();
        List<SessionEventForwardingRegister> register = sessionEventForwardingRegisterRepository.findByJurisdiction(jurisdiction);
        assertEquals(count, register.size());

    }

    @Then("^the notification request should contain valid service authorization header$")
    public void theNotificationRequestShouldContainValidServiceAuthorizationHeader() throws Throwable {
        notifierJob.execute();

        BasicJsonNotificationForwarder forwarder
            = (BasicJsonNotificationForwarder) ReflectionTestUtils.getField(notifierJob, "forwarder");

        assertNotNull(forwarder);

        String lastServiceAuthorization = forwarder.getLastServiceAuthorization();

        assertEquals(expectedMicroserviceName, authTokenValidator.getServiceName(lastServiceAuthorization));
    }
}
