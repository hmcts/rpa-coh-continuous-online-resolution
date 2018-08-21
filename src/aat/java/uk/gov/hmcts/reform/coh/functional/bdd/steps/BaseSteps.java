package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.CreateOnlineHearingResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingResponse;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohRequestEndpoint;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohRequestFactory;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestTrustManager;
import uk.gov.hmcts.reform.coh.handlers.IdamHeaderInterceptor;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingRegisterRepository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BaseSteps {

    protected RestTemplate restTemplate;

    protected static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    private Map<String, String> endpoints = new HashMap<String, String>();

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

        testContext.getHttpContext().setIdamAuthorRef("bearer judge_123_idam");
        testContext.getHttpContext().setIdamServiceRef("idam-service-ref-id");
        header = new HttpHeaders();
        header.add("Content-Type", "application/json");
        header.add(IdamHeaderInterceptor.IDAM_AUTHORIZATION, testContext.getHttpContext().getIdamAuthorRef());
        header.add(IdamHeaderInterceptor.IDAM_SERVICE_AUTHORIZATION, testContext.getHttpContext().getIdamServiceRef());
    }

    protected ResponseEntity sendRequest(String entity, String methodType, String payload) throws Exception {
        HttpMethod method = HttpMethod.valueOf(methodType);

        CohRequestEndpoint endpoint = CohRequestFactory.getRequestEndpoint(entity);
        String url = endpoint.getUrl(method, testContext);
        HttpEntity<String> request = new HttpEntity<>(payload, header);

        return restTemplate.exchange(url, method, request, String.class);
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public Map<String, String> getEndpoints() {
        return endpoints;
    }
}
