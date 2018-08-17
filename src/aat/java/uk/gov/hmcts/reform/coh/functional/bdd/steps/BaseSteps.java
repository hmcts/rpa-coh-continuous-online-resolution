package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.handlers.IdamHeaderInterceptor;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.CreateOnlineHearingResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingResponse;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestTrustManager;
import uk.gov.hmcts.reform.coh.repository.AnswerRepository;
import uk.gov.hmcts.reform.coh.repository.DecisionReplyRepository;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingPanelMemberRepository;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingRegisterRepository;
import uk.gov.hmcts.reform.coh.service.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaseSteps {
    private static final Logger log = LoggerFactory.getLogger(BaseSteps.class);

    protected RestTemplate restTemplate;

    protected static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    private Map<String, String> endpoints = new HashMap<String, String>();

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private OnlineHearingPanelMemberRepository onlineHearingPanelMemberRepository;

    @Autowired
    private DecisionService decisionService;

    @Autowired
    private DecisionReplyRepository decisionReplyRepository;

    @Autowired
    private SessionEventService sessionEventService;

    @Autowired
    private SessionEventForwardingRegisterRepository sessionEventForwardingRegisterRepository;

    @Value("${base-urls.test-url}")
    String baseUrl;

    protected TestContext testContext;
    protected HttpHeaders header;

    @Autowired
    public BaseSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    public void setup() throws Exception {
        restTemplate = new RestTemplate(TestTrustManager.getInstance().getTestRequestFactory());

        endpoints.put("online hearing", "/continuous-online-hearings");
        endpoints.put("decision", "/continuous-online-hearings/onlineHearing_id/decisions");
        endpoints.put("decisionreply", "/continuous-online-hearings/onlineHearing_id/decisionreplies");
        endpoints.put("question", "/continuous-online-hearings/onlineHearing_id/questions");
        endpoints.put("answer", "/continuous-online-hearings/onlineHearing_id/questions/question_id/answers");
        endpoints.put("conversations", "/continuous-online-hearings/onlineHearing_id/conversations");

        Iterable<SessionEventForwardingRegister> sessionEventForwardingRegisters = sessionEventForwardingRegisterRepository.findAll();

        sessionEventForwardingRegisters.iterator().forEachRemaining(
                sefr -> sefr.setForwardingEndpoint(sefr.getForwardingEndpoint().replace("${base-urls.test-url}", baseUrl).replace("https", "http")));
        sessionEventForwardingRegisterRepository.saveAll(sessionEventForwardingRegisters);

        testContext.getScenarioContext().setIdamAuthorRef("bearer judge_123_idam");
        testContext.getScenarioContext().setIdamServiceRef("idam-service-ref-id");
        header = new HttpHeaders();
        header.add("Content-Type", "application/json");
        header.add(IdamHeaderInterceptor.IDAM_AUTHORIZATION, testContext.getScenarioContext().getIdamAuthorRef());
        header.add(IdamHeaderInterceptor.IDAM_SERVICE_AUTHORIZATION, testContext.getScenarioContext().getIdamServiceRef());
    }

    public void cleanup() {
        for(DecisionReply decisionReply : testContext.getScenarioContext().getDecisionReplies()) {
            try {
                decisionReplyRepository.deleteById(decisionReply.getId());
            }catch (Exception e) {
                log.error("Failure may be due to foreign key. This is okay because the online hearing will be deleted elsewhere.");
            }
        }
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
