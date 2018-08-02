package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.CreateOnlineHearingResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingResponse;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingPanelMemberRepository;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingRegisterRepository;
import uk.gov.hmcts.reform.coh.service.DecisionService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.SessionEventService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BaseSteps {
    private static final Logger log = LoggerFactory.getLogger(BaseSteps.class);

    @Autowired
    protected RestTemplate restTemplate;

    protected static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    private Map<String, String> endpoints = new HashMap<String, String>();

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private OnlineHearingPanelMemberRepository onlineHearingPanelMemberRepository;

    @Autowired
    private DecisionService decisionService;

    @Autowired
    private SessionEventService sessionEventService;

    @Autowired
    private SessionEventForwardingRegisterRepository sessionEventForwardingRegisterRepository;

    @Value("${base-urls.test-url}")
    String baseUrl;

    protected TestContext testContext;

    @Autowired
    public BaseSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    public void setup() throws Exception {
        endpoints.put("online hearing", "/continuous-online-hearings");
        endpoints.put("decision", "/continuous-online-hearings/onlineHearing_id/decisions");
        endpoints.put("question", "/continuous-online-hearings/onlineHearing_id/questions");
        endpoints.put("answer", "/continuous-online-hearings/onlineHearing_id/questions/question_id/answers");

        Iterable<SessionEventForwardingRegister> sessionEventForwardingRegisters = sessionEventForwardingRegisterRepository.findAll();

        sessionEventForwardingRegisters.iterator().forEachRemaining(
                sefr -> sefr.setForwardingEndpoint(sefr.getForwardingEndpoint().replace("${base-urls.test-url}", baseUrl)));
        sessionEventForwardingRegisterRepository.saveAll(sessionEventForwardingRegisters);
    }

    public void cleanup() {
        if(testContext.getScenarioContext().getSessionEventForwardingRegisters() != null) {
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

        // Delete all online hearing + panel members
        if (testContext.getScenarioContext().getCaseIds() != null) {

            for (String caseId : testContext.getScenarioContext().getCaseIds()) {
                try {
                    OnlineHearing onlineHearing = new OnlineHearing();
                    onlineHearing.setCaseId(caseId);
                    onlineHearing = onlineHearingService.retrieveOnlineHearingByCaseId(onlineHearing);

                    // First delete event linked to an online hearing
                    sessionEventService.deleteByOnlineHearing(onlineHearing);

                    // Now delete the panel members
                    onlineHearingPanelMemberRepository.deleteByOnlineHearing(onlineHearing);
                    onlineHearingService.deleteByCaseId(caseId);
                } catch (DataIntegrityViolationException e) {
                    log.error("Failure may be due to foreign key. This is okay because the online hearing will be deleted elsewhere.");
                }
            }
        }
    }

    OnlineHearing createOnlineHearingFromResponse(CreateOnlineHearingResponse response) {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(UUID.fromString(response.getOnlineHearingId()));

        return onlineHearing;
    }

    OnlineHearing createOnlineHearingFromResponse(OnlineHearingResponse response) {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(response.getOnlineHearingId());

        return onlineHearing;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public Map<String, String> getEndpoints() {
        return endpoints;
    }
}
