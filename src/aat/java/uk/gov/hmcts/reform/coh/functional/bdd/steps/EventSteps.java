package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.coh.controller.events.EventRegistrationRequest;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingRegisterRepository;
import uk.gov.hmcts.reform.coh.repository.SessionEventTypeRespository;

import java.io.IOException;
import java.util.Optional;

public class EventSteps extends BaseSteps {
    private static final Logger log = LoggerFactory.getLogger(EventSteps.class);

    private ResponseEntity<String> response;
    private String endpoint = "/continuous-online-hearings/events/register";

    @Autowired
    private JurisdictionRepository jurisdictionRepository;
    @Autowired
    private SessionEventForwardingRegisterRepository sessionEventForwardingRegisterRepository;
    @Autowired
    private SessionEventTypeRespository sessionEventTypeRespository;

    private Jurisdiction jurisdiction;

    @Autowired
    public EventSteps(TestContext testContext) {
        super(testContext);
        restTemplate = getRestTemplate();
    }

    @Before
    public void setUp() throws Exception {
        super.setup();
        jurisdiction = new Jurisdiction();
    }

    @After("@events")
    public void jurisdictionCleanUp() {

        Optional<SessionEventType> sessionEventType = sessionEventTypeRespository.findByEventTypeName("question_round_issued");


        Optional<SessionEventForwardingRegister> sessionEventForwardingRegister = sessionEventForwardingRegisterRepository
                .findByJurisdictionAndSessionEventType(jurisdiction, sessionEventType.get());

        sessionEventForwardingRegister.ifPresent(sessionEventForwardingRegister1 -> sessionEventForwardingRegisterRepository.delete(sessionEventForwardingRegister1));
    }

    @And("^jurisdiction ' \"([^\"]*)\", with id ' \"(\\d+)\" ' and max question rounds ' \"(\\d+)\" ' is created$")
    public void aJurisdictionNamedWithUrlAndMaxQuestionRoundsIsCreated(String jurisdictionName, Long id,  int maxQuestionRounds) {
        jurisdiction = new Jurisdiction();

        jurisdiction.setJurisdictionId(id);
        jurisdiction.setJurisdictionName(jurisdictionName);
        jurisdiction.setMaxQuestionRounds(maxQuestionRounds);
        jurisdictionRepository.save(jurisdiction);
        testContext.getScenarioContext().getEventRegistrationRequest().setJurisdiction(jurisdiction.getJurisdictionName());
    }

    @Given("^a conflicting request to subscribe to question round issued$")
    public void aConflictingRequestToSubscribeToQuestionRoundIssued() throws IOException {
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
        try {
            response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.POST, request, String.class);
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
            testContext.getHttpContext().setHttpResponseStatusCode(response.getStatusCodeValue());
        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setResponseBodyAndStatesForException(hcee);
        }
    }

    @And("^a standard event register request$")
    public void aStandardEventRegisterRequest() throws IOException{
        String json = JsonUtils.getJsonInput("event_forwarding_register/subscribe_to_qr_issued");
        EventRegistrationRequest eventRegistrationRequest = (EventRegistrationRequest) JsonUtils.toObjectFromJson(json, EventRegistrationRequest.class);
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
