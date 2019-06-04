package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.CreateOnlineHearingResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingRequest;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.idam.IdamAuthentication;
import uk.gov.hmcts.reform.coh.repository.*;
import uk.gov.hmcts.reform.coh.schedule.notifiers.EventNotifierJob;
import uk.gov.hmcts.reform.coh.service.*;
import uk.gov.hmcts.reform.coh.states.SessionEventForwardingStates;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.StreamSupport;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@ContextConfiguration
@SpringBootTest
@ActiveProfiles("cucumber")
public class ApiSteps extends BaseSteps {

    private static final Logger log = LoggerFactory.getLogger(ApiSteps.class);

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private JurisdictionRepository jurisdictionRepository;

    @Autowired
    private SessionEventForwardingRegisterRepository sessionEventForwardingRegisterRepository;

    @Autowired
    private SessionEventTypeRespository sessionEventTypeRespository;

    @Autowired
    private SessionEventService sessionEventService;

    @Autowired
    private EventNotifierJob eventNotifierJob;

    @Autowired
    private OnlineHearingRepository onlineHearingRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private DecisionService decisionService;

    @Autowired
    private DecisionReplyRepository decisionReplyRepository;

    private JSONObject json;

    private Map<SessionEventForwardingRegister, String> originalSettings = new HashMap<>();

    @Autowired
    public ApiSteps(TestContext testContext, IdamAuthentication idamAuthentication) {
        super(testContext, idamAuthentication);
    }

    @Before
    public void setUp() throws Exception {
        super.setup();

        // For testing purposes, we want to hit the dummy notification endpoint
        Iterable<SessionEventForwardingRegister> sessionEventForwardingRegisters = sessionEventForwardingRegisterRepository.findAll();
        sessionEventForwardingRegisters.iterator()
                .forEachRemaining(
                        sefr -> {
                            originalSettings.put(sefr, sefr.getForwardingEndpoint());
                            sefr.setForwardingEndpoint(testNotificationUrl.replace("${base-urls.test-url}", baseUrl).replace("https", "http"));
                        });
        sessionEventForwardingRegisterRepository.saveAll(sessionEventForwardingRegisters);
    }

    @After
    public void cleanUp() {

        // Set the forwarding endpoints back to original
        originalSettings.forEach( (k, v) -> k.setForwardingEndpoint(v));
        sessionEventForwardingRegisterRepository.saveAll(originalSettings.keySet());

        /**
         * Now start cleaning up test data
         */
        for (DecisionReply decisionReply : testContext.getScenarioContext().getDecisionReplies()) {
            try {
                decisionReplyRepository.deleteById(decisionReply.getId());
            } catch (Exception e) {
                log.error("Failure may be due to foreign key. This is okay because the online hearing will be deleted elsewhere.");
            }
        }

        if (testContext.getScenarioContext().getSessionEventForwardingRegisters() != null) {
            for (SessionEventForwardingRegister sessionEventForwardingRegister : testContext.getScenarioContext().getSessionEventForwardingRegisters()) {
                try {
                    sessionEventForwardingRegisterRepository.delete(sessionEventForwardingRegister);
                } catch (DataIntegrityViolationException e) {
                    log.error("Failure may be due to foreign key. This is okay because the online hearing will be deleted elsewhere.");
                }
            }
        }

        // Delete all decisions
        if (testContext.getScenarioContext().getCurrentDecision() != null) {
            Decision decision = testContext.getScenarioContext().getCurrentDecision();
            try {
                decisionService.deleteDecisionById(decision.getDecisionId());
            }
            catch (Exception e) {
                log.debug("Unable to delete decision: " + decision.getDecisionId());
            }
        }

        if (testContext.getScenarioContext().getCaseIds() != null) {

            for (String caseId : testContext.getScenarioContext().getCaseIds()) {
                try {
                    OnlineHearing onlineHearing = new OnlineHearing();
                    onlineHearing.setCaseId(caseId);
                    onlineHearing = onlineHearingService.retrieveOnlineHearingByCaseId(onlineHearing);

                    // Delete all the Q & A
                    Optional<List<Question>> questionList = questionService.findAllQuestionsByOnlineHearing(onlineHearing);
                    if (questionList.isPresent()) {
                        for (Question question : questionList.get()) {
                            List<Answer> answers = answerService.retrieveAnswersByQuestion(question);
                            if (!answers.isEmpty()) {
                                for (Answer answer : answers) {
                                    answerRepository.delete(answer);
                                }
                            }
                            questionService.deleteQuestion(question);
                        }
                    }

                    // First delete event linked to an online hearing
                    sessionEventService.deleteByOnlineHearing(onlineHearing);

                    // Now delete online hearing
                    onlineHearingService.deleteByCaseId(caseId);
                } catch (DataIntegrityViolationException e) {
                    log.error("Failure may be due to foreign key. This is okay because the online hearing will be deleted elsewhere.");
                }
            }
        }

        List<Jurisdiction> jurisdictions = testContext.getScenarioContext().getJurisdictions();
        if (jurisdictions != null && !jurisdictions.isEmpty()) {
            for (Jurisdiction jurisdiction : jurisdictions) {
                try {
                    StreamSupport.stream(sessionEventForwardingRegisterRepository.findByJurisdiction(jurisdiction).spliterator(), false)
                            .forEach(sessionEventForwardingRegisterRepository::delete);
                    jurisdictionRepository.delete(jurisdiction);
                } catch (DataIntegrityViolationException e) {
                    log.error(
                            "Failure may be due to foreign key. This is okay because the online hearing will be deleted elsewhere.");
                }
            }
        }
    }

    @When("^a post request is sent to ' \"([^\"]*)\"'$")
    public void a_post_request_is_sent_to(String endpoint) {
        HttpEntity<String> request = new HttpEntity<>(json.toString(), header);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.POST, request, String.class);
        testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
    }

    @Then("^the response code is (\\d+)$")
    public void the_response_code_is(int responseCode) {
        assertEquals("Response status code", responseCode, testContext.getHttpContext().getHttpResponseStatusCode());
    }

    @Then("^the response contains the following text '\"([^\"]*)\" '$")
    public void the_response_contains_the_following_text(String text) {
        assertTrue(testContext.getHttpContext().getRawResponseString().contains(text));
    }

    @Then("^the response contains the online hearing UUID$")
    public void the_response_contains_the_online_hearing_UUID() throws IOException {
        String responseString = testContext.getHttpContext().getRawResponseString();
        CreateOnlineHearingResponse response = JsonUtils
            .toObjectFromJson(responseString, CreateOnlineHearingResponse.class);
        assertEquals(response.getOnlineHearingId(), UUID.fromString(response.getOnlineHearingId()).toString());
    }

    @Given("^a standard online hearing is created$")
    public void aStandardOnlineHearingIsCreated() throws Throwable {
        String jsonBody = JsonUtils.getJsonInput("online_hearing/standard_online_hearing");

        OnlineHearingRequest onlineHearingRequest = JsonUtils.toObjectFromJson(jsonBody, OnlineHearingRequest.class);
        HttpEntity<String> request = new HttpEntity<>(jsonBody, header);
        try {
            ResponseEntity<String> response = restTemplate
                .exchange(baseUrl + "/continuous-online-hearings", HttpMethod.POST, request, String.class);
            String responseString = response.getBody();

            System.out.println(String.format("XXXXX %s", responseString));

            testContext.getScenarioContext().setCurrentOnlineHearing(onlineHearingRequest);
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);

            CreateOnlineHearingResponse newOnlineHearing = JsonUtils
                .toObjectFromJson(responseString, CreateOnlineHearingResponse.class);
            testContext.getScenarioContext().getCurrentOnlineHearing()
                .setOnlineHearingId(UUID.fromString(newOnlineHearing.getOnlineHearingId()));
            testContext.getScenarioContext().addCaseId(onlineHearingRequest.getCaseId());

            testContext.getScenarioContext()
                .setCurrentOnlineHearing(onlineHearingRepository.findByCaseId(onlineHearingRequest.getCaseId()).get());
        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(hcee);
        }
    }

    @And("^the online hearing jurisdiction is ' \"([^\"]*)\" '$")
    public void theOnlineHearingJurisdictionIsSCSS(String jurisdictionName) {
        testContext.getScenarioContext().getCurrentOnlineHearingRequest().setJurisdiction(jurisdictionName);
    }

    @And("^the post request is sent to create the online hearing$")
    public void thePostRequestIsSentToCreateTheOnlineHearing() throws IOException {
        String jsonBody = JsonUtils.toJson(testContext.getScenarioContext().getCurrentOnlineHearingRequest());
        HttpEntity<String> request = new HttpEntity<>(jsonBody, header);
        ResponseEntity<String> response = restTemplate
            .exchange(baseUrl + "/continuous-online-hearings", HttpMethod.POST, request, String.class);
        String responseString = response.getBody();
        testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        CreateOnlineHearingResponse newOnlineHearing = JsonUtils
            .toObjectFromJson(responseString, CreateOnlineHearingResponse.class);
        testContext.getScenarioContext().setCurrentOnlineHearing(new OnlineHearing());
        testContext.getScenarioContext().getCurrentOnlineHearing()
            .setOnlineHearingId(UUID.fromString(newOnlineHearing.getOnlineHearingId()));
        testContext.getScenarioContext().setCurrentOnlineHearing(onlineHearingRepository
            .findByCaseId(testContext.getScenarioContext().getCurrentOnlineHearingRequest().getCaseId()).get());

    }

    @And("^a jurisdiction named ' \"([^\"]*)\", with id ' \"(\\d+)\" ' and max question rounds ' \"(\\d+)\" ' is created$")
    public void aJurisdictionNamedWithUrlAndMaxQuestionRoundsIsCreated(String jurisdictionName, Long id,
        int maxQuestionRounds) {
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setJurisdictionId(id);
        jurisdiction.setJurisdictionName(jurisdictionName);
        jurisdiction.setMaxQuestionRounds(maxQuestionRounds);
        jurisdictionRepository.save(jurisdiction);
        testContext.getScenarioContext().addJurisdiction(jurisdiction);
        testContext.getScenarioContext().setCurrentJurisdiction(jurisdiction);
    }


    @And("^the jurisdiction is registered to receive ([^\"]*) events$")
    public void theJurisdictionIsRegisteredToReceiveQuestionRoundIssuedEvents(String eventType) {

        SessionEventType sessionEventType = sessionEventTypeRespository.findByEventTypeName(eventType)
            .orElseThrow(EntityNotFoundException::new);
        Jurisdiction testJurisdiction = jurisdictionRepository.findByJurisdictionName("SSCS")
            .orElseThrow(EntityNotFoundException::new);
        SessionEventForwardingRegister templateEFR = sessionEventForwardingRegisterRepository
            .findByJurisdictionAndSessionEventType(testJurisdiction, sessionEventType)
            .orElseThrow(EntityNotFoundException::new);

        SessionEventForwardingRegister sessionEventForwardingRegister = new SessionEventForwardingRegister.Builder()
            .jurisdiction(testContext.getScenarioContext().getCurrentJurisdiction())
            .sessionEventType(sessionEventType)
            .forwardingEndpoint(templateEFR.getForwardingEndpoint())
            .maximumRetries(templateEFR.getMaximumRetries())
            .registrationDate(new Date())
            .withActive(true)
            .build();

        SessionEventForwardingRegister savedEFR = sessionEventForwardingRegisterRepository
            .save(sessionEventForwardingRegister);
        testContext.getScenarioContext().addSessionEventForwardingRegister(savedEFR);
    }

    @And("^the response headers contains a location to the created entity$")
    public void theHeaderContainsLocationOfCreatedQuestion() {
        ResponseEntity responseEntity = testContext.getHttpContext().getResponseEntity();
        HttpHeaders headers = responseEntity.getHeaders();
        assertFalse(headers.get("Location").isEmpty());
    }

    @And("^send get request to the location$")
    public void sendGetRequestToTheLocation() {
        ResponseEntity responseEntity = testContext.getHttpContext().getResponseEntity();
        HttpHeaders headers = responseEntity.getHeaders();
        String urlToLocation = headers.get("Location").get(0);

        HttpEntity<String> request = new HttpEntity<>("", header);
        ResponseEntity<String> response = restTemplate.exchange(urlToLocation, HttpMethod.GET, request, String.class);
        testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
    }

    @When("^an event has been queued for this online hearing of event type (.*)$")
    public void anEventHasBeenQueuedForThisOnlineHearingOfEventType(String eventType) {
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        List<SessionEvent> sessionEvents = sessionEventService.retrieveByOnlineHearing(onlineHearing);

        assertFalse(sessionEvents.isEmpty());
        boolean hasEvent = sessionEvents.stream()
            .anyMatch(se -> se.getSessionEventForwardingRegister().getSessionEventType().getEventTypeName()
                .equalsIgnoreCase(eventType));
        assertTrue(hasEvent);
    }

    @And("^there is no event queued for this online hearing of event type (.*)")
    public void thereIsNoEventQueuedForThisOnlineHearingOfEventTypeAnswersSubmitted(String eventType) throws Throwable {
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        List<SessionEvent> sessionEvents = sessionEventService.retrieveByOnlineHearing(onlineHearing);

        boolean hasEvent = sessionEvents.stream()
            .noneMatch(se -> se.getSessionEventForwardingRegister().getSessionEventType().getEventTypeName()
                .equalsIgnoreCase(eventType));
        assertTrue(hasEvent);
    }

    @And("^the event has been set to (.*) of event type (.*)$")
    public void thePutRequestIsSentToResetTheEventsOfTypeAnswerSubmitted(String forwardingState, String eventType) {
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        List<SessionEvent> sessionEvents = sessionEventService.retrieveByOnlineHearing(onlineHearing);

        long count = sessionEvents.stream()
                .filter(se -> se.getSessionEventForwardingRegister().getSessionEventType().getEventTypeName().equalsIgnoreCase(eventType) &&
                        se.getSessionEventForwardingState().getForwardingStateName().equalsIgnoreCase(forwardingState)
                )
                .count();

        assertTrue(count == 1);
    }

    @And("^the event type (.*) has been set to retries of (\\d+)$")
    public void theEventHasBeenSetToRetriesOf(String eventType, int expectedRetries) {
        SessionEventType expectedEventType = sessionEventTypeRespository.findByEventTypeName(eventType)
            .orElseThrow(EntityNotFoundException::new);

        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        Jurisdiction jurisdiction = jurisdictionRepository
            .findByJurisdictionName(onlineHearing.getJurisdiction().getJurisdictionName())
            .orElseThrow(EntityNotFoundException::new);

        SessionEventForwardingRegisterId sessionEventForwardingRegisterId = new SessionEventForwardingRegisterId(
            jurisdiction.getJurisdictionId(), expectedEventType.getEventTypeId());

        List<SessionEvent> sessionEvents = sessionEventService.retrieveByOnlineHearing(onlineHearing);
        boolean hasExpectedRetries = sessionEvents.stream()
            .filter(se -> se.getSessionEventForwardingRegister().getEventForwardingRegisterId()
                .equals(sessionEventForwardingRegisterId))
            .allMatch(se -> se.getRetries() == expectedRetries);

        assertTrue(hasExpectedRetries);
    }

    @When("^(\\d+) (.+) events? (?:are|is) added$")
    public void answer_submittedEventsAreTriggered(int number, String eventType) {
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        for (int i = 0; i < number; i++) {
            sessionEventService.createSessionEvent(onlineHearing, eventType);
        }
    }

    @Then("there (?:are|is) (\\d+) (.+) events? in the queue")
    public void there_are_x_event_types_in_the_queue(int number, String eventType) {
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        List<SessionEvent> sessionEvents = sessionEventService.retrieveByOnlineHearing(onlineHearing);
        long actual = sessionEvents.stream()
            .filter(sessionEvent -> eventType
                .equals(sessionEvent.getSessionEventForwardingRegister().getSessionEventType().getEventTypeName()))
            .filter(sessionEvent -> SessionEventForwardingStates.EVENT_FORWARDING_PENDING.getStateName()
                .equals(sessionEvent.getSessionEventForwardingState().getForwardingStateName()))
            .count();

        assertEquals(number, actual);
    }
}
