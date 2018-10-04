package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohEndpointFactory;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohEndpointHandler;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohEntityTypes;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestTrustManager;
import uk.gov.hmcts.reform.coh.handlers.IdamHeaderInterceptor;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingRegisterRepository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class BaseSteps {

    protected RestTemplate restTemplate;

    protected static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private SessionEventForwardingRegisterRepository sessionEventForwardingRegisterRepository;

    @Value("${base-urls.test-url}")
    String baseUrl;

    @Value("${aat.test-notification-endpoint}")
    String testNotificationUrl;

    protected TestContext testContext;
    
    protected HttpHeaders header;

    @Autowired
    public BaseSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    public void setup() throws Exception {
        restTemplate = new RestTemplate(TestTrustManager.getInstance().getTestRequestFactory());

        testContext.getHttpContext().setIdamAuthorRef("bearer judge_123_idam");
        testContext.getHttpContext().setIdamServiceRef("idam-service-ref-id");
        header = new HttpHeaders();
        header.add("Content-Type", "application/json");
        header.add(IdamHeaderInterceptor.IDAM_AUTHORIZATION, testContext.getHttpContext().getIdamAuthorRef());
        header.add(IdamHeaderInterceptor.IDAM_SERVICE_AUTHORIZATION, testContext.getHttpContext().getIdamServiceRef());
    }

    protected ResponseEntity<String> sendRequest(CohEntityTypes entity, String methodType, String payload) {
        return sendRequest(entity.toString(), methodType, payload);
    }

    protected ResponseEntity<String> sendRequest(String entity, String methodType, String payload) {
        HttpMethod method = HttpMethod.valueOf(methodType);

        CohEndpointHandler endpoint = CohEndpointFactory.getRequestEndpoint(entity);
        return sendRequest(endpoint.getUrl(method, testContext), method, payload);
    }

    protected ResponseEntity<String> sendRequest(String url, HttpMethod method, String payload) {
        HttpEntity<String> request = new HttpEntity<>(payload, header);
        return restTemplate.exchange(url, method, request, String.class);
    }

    protected ResponseEntity<String> sendRequest(String url, HttpMethod method) {
        return sendRequest(url, method, null);
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}
