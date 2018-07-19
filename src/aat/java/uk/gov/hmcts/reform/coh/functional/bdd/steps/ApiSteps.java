package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.CreateOnlineHearingResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingRequest;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestTrustManager;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingPanelMemberRepository;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;

import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@ContextConfiguration
@SpringBootTest
public class ApiSteps extends BaseSteps {
    private static final Logger log = LoggerFactory.getLogger(ApiSteps.class);

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private JurisdictionRepository jurisdictionRepository;

    @Autowired
    private OnlineHearingPanelMemberRepository onlineHearingPanelMemberRepository;

    private JSONObject json;

    private CloseableHttpClient httpClient;
    private HttpHeaders header;
    private RestTemplate restTemplate;

    private Set<String> caseIds;
    private Set<Jurisdiction> jurisdictions;

    @Autowired
    public ApiSteps(TestContext testContext) {
        super(testContext);
    }

    @Before
    public void setup() throws Exception {
        caseIds = new HashSet<String>();
        httpClient = HttpClientBuilder
                .create()
                .setSslcontext(new SSLContextBuilder()
                        .loadTrustMaterial(null, TestTrustManager.getInstance().getTrustStrategy())
                        .build())
                .build();
        header = new HttpHeaders();
        header.add("Content-Type", "application/json");
        restTemplate = new RestTemplate(TestTrustManager.getInstance().getTestRequestFactory());
        jurisdictions = new HashSet<>();
        testContext.getScenarioContext().setJurisdictions(jurisdictions);
    }

    @After
    public void cleanUp() {
        for (String caseId : caseIds) {
            try {
                OnlineHearing onlineHearing = new OnlineHearing();
                onlineHearing.setCaseId(caseId);
                onlineHearing = onlineHearingService.retrieveOnlineHearingByCaseId(onlineHearing);
                onlineHearingPanelMemberRepository.deleteByOnlineHearing(onlineHearing);
                onlineHearingService.deleteByCaseId(caseId);
            }catch(DataIntegrityViolationException e){
                log.error("Failure may be due to foreign key. This is okay because the online hearing will be deleted elsewhere.");
            }
        }

        for(Jurisdiction jurisdiction : testContext.getScenarioContext().getJurisdictions()){
            try {
                jurisdictionRepository.delete(jurisdiction);
            }catch(DataIntegrityViolationException e){
                log.error("Failure may be due to foreign key. This is okay because the online hearing will be deleted elsewhere.");
            }
        }
    }

    @When("^a get request is sent to ' \"([^\"]*)\"' for the saved online hearing$")
    public void a_get_request_is_sent_to(String endpoint) throws Throwable {
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        HttpGet request = new HttpGet(baseUrl + endpoint + "/" + onlineHearing.getOnlineHearingId().toString());
        request.addHeader("content-type", "application/json");

        testContext.getHttpContext().setResponseBodyAndStatesForResponse(httpClient.execute(request));
    }

    @When("^a get request is sent to ' \"([^\"]*)\"' for the online hearing$")
    public void a_filter_get_request_is_sent_to(String endpoint) throws Throwable {
        HttpGet request = new HttpGet(baseUrl + endpoint);
        request.addHeader("content-type", "application/json");

        testContext.getHttpContext().setResponseBodyAndStatesForResponse(httpClient.execute(request));
    }

    @When("^a post request is sent to ' \"([^\"]*)\"'$")
    public void a_post_request_is_sent_to(String endpoint) throws Throwable {
        HttpPost request = new HttpPost(baseUrl + endpoint);
        request.addHeader("content-type", "application/json");
        StringEntity params = new StringEntity(json.toString());
        request.setEntity(params);
        testContext.getHttpContext().setResponseBodyAndStatesForResponse(httpClient.execute(request));
    }

    @Then("^the response code is (\\d+)$")
    public void the_response_code_is(int responseCode) throws Throwable {
        assertEquals("Response status code", responseCode, testContext.getHttpContext().getHttpResponseStatusCode());
    }

    @Then("^the response contains the following text '\"([^\"]*)\" '$")
    public void the_response_contains_the_following_text(String text) throws IOException {
        assertTrue(testContext.getHttpContext().getRawResponseString().contains(text));
    }

    @Then("^the response contains the online hearing UUID$")
    public void the_response_contains_the_online_hearing_UUID() throws IOException {
        String responseString = testContext.getHttpContext().getRawResponseString();
        CreateOnlineHearingResponse response = (CreateOnlineHearingResponse) JsonUtils.toObjectFromJson(responseString, CreateOnlineHearingResponse.class);
        assertEquals(response.getOnlineHearingId(), UUID.fromString(response.getOnlineHearingId()).toString());
    }

    @Given("^a standard online hearing is created$")
    public void aStandardOnlineHearingIsCreated() throws Throwable {
        String jsonBody = JsonUtils.getJsonInput("online_hearing/standard_online_hearing");

        OnlineHearingRequest onlineHearingRequest = (OnlineHearingRequest)JsonUtils.toObjectFromJson(jsonBody, OnlineHearingRequest.class);
        HttpEntity<String> request = new HttpEntity<>(jsonBody, header);
        try {
            ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/continuous-online-hearings", HttpMethod.POST, request, String.class);
            String responseString = response.getBody();
            testContext.getScenarioContext().setCurrentOnlineHearing(onlineHearingRequest);
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);

            CreateOnlineHearingResponse newOnlineHearing = (CreateOnlineHearingResponse) JsonUtils.toObjectFromJson(responseString, CreateOnlineHearingResponse.class);
            testContext.getScenarioContext().getCurrentOnlineHearing().setOnlineHearingId(UUID.fromString(newOnlineHearing.getOnlineHearingId()));
            testContext.getScenarioContext().addCaseId(onlineHearingRequest.getCaseId());
        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setResponseBodyAndStatesForException(hcee);
        }
    }

    @And("^the online hearing jurisdiction is ' \"([^\"]*)\" '$")
    public void theOnlineHearingJurisdictionIsSCSS(String jurisdictionName){
        testContext.getScenarioContext().getCurrentOnlineHearingRequest().setJurisdiction(jurisdictionName);
    }

    @And("^the post request is sent to create the online hearing$")
    public void thePostRequestIsSentToCreateTheOnlineHearing() throws IOException {
        String jsonBody = JsonUtils.toJson(testContext.getScenarioContext().getCurrentOnlineHearingRequest());
        HttpEntity<String> request = new HttpEntity<>(jsonBody, header);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/continuous-online-hearings", HttpMethod.POST, request, String.class);
        String responseString = response.getBody();
        testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);

        CreateOnlineHearingResponse newOnlineHearing = (CreateOnlineHearingResponse)JsonUtils.toObjectFromJson(responseString, CreateOnlineHearingResponse.class);
        testContext.getScenarioContext().getCurrentOnlineHearing().setOnlineHearingId(UUID.fromString(newOnlineHearing.getOnlineHearingId()));
    }

    @And("^^a jurisdiction named ' \"([^\"]*)\", with id ' \"(\\d+)\" ' with url ' \"([^\"]*)\" and max question rounds ' \"(\\d+)\" ' is created$$")
    public void aJurisdictionNamedWithUrlAndMaxQuestionRoundsIsCreated(String jurisdictionName, Long id, String url, int maxQuestionRounds) {
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setJurisdictionId(id);
        jurisdiction.setJurisdictionName(jurisdictionName);
        jurisdiction.setUrl(url);
        jurisdiction.setMaxQuestionRounds(maxQuestionRounds);
        jurisdictionRepository.save(jurisdiction);
        jurisdictions.add(jurisdiction);
    }

    @And("^the response contains (\\d+) panel member$")
    public void theResponseContainsPanelMember(int count) throws IOException {
        String rawResponseString = testContext.getHttpContext().getRawResponseString();
        ObjectMapper objMap = new ObjectMapper();
        Map<String, Object> map = objMap.readValue(rawResponseString, new TypeReference<Map<String, Object>>() {
        });

        List<String> map1 = (List<String>) map.get("panel");
        assertEquals(count, map1.size());
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
        testContext.getHttpContext().setHttpResponseStatusCode(response.getStatusCodeValue());
        testContext.getHttpContext().setRawResponseString(response.getBody());
    }
}