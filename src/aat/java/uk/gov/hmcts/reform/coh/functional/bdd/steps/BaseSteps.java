package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohEndpointFactory;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohEndpointHandler;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohEntityTypes;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestTrustManager;
import uk.gov.hmcts.reform.coh.handlers.IdamHeaderInterceptor;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingRegisterRepository;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.fail;

public class BaseSteps {

    protected RestTemplate restTemplate;

    protected static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private SessionEventForwardingRegisterRepository sessionEventForwardingRegisterRepository;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Value("${base-urls.test-url}")
    protected String baseUrl;

    @Value("${aat.test-notification-endpoint}")
    protected String testNotificationUrl;

    @Value("${base-urls.idam-url}")
    protected String idamUrl;

    @Value("${base-urls.idam-user-email}")
    private String idamEmail;

    @Value("${base-urls.idam-user-role}")
    protected String idamUserRole;

    protected TestContext testContext;
    
    protected HttpHeaders header;

    protected Integer idamUser;

    @Autowired
    public BaseSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    public void setup() throws Exception {
        restTemplate = new RestTemplate(TestTrustManager.getInstance().getTestRequestFactory());

        prepareAuthenticationTokens();

        header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        Optional.ofNullable(testContext.getHttpContext().getIdamAuthorRef())
            .ifPresent(token -> header.add(IdamHeaderInterceptor.IDAM_AUTHORIZATION, "Bearer " + token));

        Optional.ofNullable(testContext.getHttpContext().getIdamServiceRef())
            .ifPresent(token -> header.add(IdamHeaderInterceptor.IDAM_SERVICE_AUTHORIZATION, token));
    }

    protected static void withValidHttpCodes(Consumer<RestTemplate> consumer, int... codes) {
        withValidHttpCodes(new RestTemplate(), consumer, codes);
    }

    protected static void withValidHttpCodes(RestTemplate rt, Consumer<RestTemplate> consumer, int... codes) {
        ResponseErrorHandler oldErrorHandler = rt.getErrorHandler();
        rt.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                for (int code : codes) {
                    if (response.getRawStatusCode() == code) {
                        return false;
                    }
                }
                return oldErrorHandler.hasError(response);
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                oldErrorHandler.handleError(response);
            }
        });
        consumer.accept(rt);
    }

    private void prepareAuthenticationTokens() throws Exception {
        findIdamUserIdByEmail();
        registerIfNecessary();
        getIdamToken();
        getS2sToken();
    }

    private void findIdamUserIdByEmail() {
        String usersUri = UriComponentsBuilder.fromUriString(idamUrl)
            .path("/users")
            .queryParam("email", idamEmail)
            .toUriString();

        withValidHttpCodes(
            rt -> {
                ResponseEntity<Map> entity = rt.getForEntity(usersUri, Map.class);
                if (entity.getStatusCode() == HttpStatus.OK && entity.hasBody() && entity.getBody().containsKey("id")) {
                    idamUser = (Integer) entity.getBody().get("id");
                }
            },
            404
        );
    }

    private void registerIfNecessary() throws JsonProcessingException {
        if (idamUser != null) {
            return;
        }
        String idamPassword = UUID.randomUUID().toString();
        ImmutableMap<String, Object> body = ImmutableMap.of(
            "email", idamEmail,
            "password", idamPassword,
            "forename", "test",
            "surname", "test",
            "roles", ImmutableList.of(
                ImmutableMap.of(
                    "code", idamUserRole
                )
            )
        );
        String json = JsonUtils.toJson(body);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setOutputStreaming(false);
        withValidHttpCodes(
            new RestTemplate(simpleClientHttpRequestFactory),
            rt -> {
                ResponseEntity<String> responseEntity = rt
                    .postForEntity(idamUrl + "/testing-support/accounts", request, String.class);

                if (responseEntity.getStatusCodeValue() != 204) {
                    fail("Could not create account in IdAM");
                } else {
                    findIdamUserIdByEmail();
                }
            },
            401
        );
    }

    private void getIdamToken() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.set("id", idamUser.toString());
        body.set("role", idamUserRole);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> idamResponse = new RestTemplate()
            .postForEntity(idamUrl + "/testing-support/lease", request, String.class);

        if (idamResponse.hasBody()) {
            testContext.getHttpContext().setIdamAuthorRef(idamResponse.getBody());
        }
    }

    private void getS2sToken() {
        testContext.getHttpContext().setIdamServiceRef(authTokenGenerator.generate());
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
