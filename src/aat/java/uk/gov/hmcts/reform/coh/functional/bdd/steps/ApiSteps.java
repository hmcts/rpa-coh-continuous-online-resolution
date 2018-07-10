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
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.CreateOnlinehearingResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlinehearingRequest;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.Onlinehearing;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestTrustManager;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;
import uk.gov.hmcts.reform.coh.repository.OnlinehearingPanelMemberRepository;
import uk.gov.hmcts.reform.coh.service.OnlinehearingService;

import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@ContextConfiguration
@SpringBootTest
public class ApiSteps extends BaseSteps {
    private static final Logger log = LoggerFactory.getLogger(ApiSteps.class);

    @Autowired
    private OnlinehearingService onlinehearingService;

    @Autowired
    private JurisdictionRepository jurisdictionRepository;

    @Autowired
    private OnlinehearingPanelMemberRepository onlinehearingPanelMemberRepository;

    private JSONObject json;

    private CloseableHttpClient httpClient;
    private HttpHeaders header;
    private RestTemplate restTemplate;

    private Set<String> caseIds;
    private Set<Jurisdiction> jurisdictions;
    private TestContext testContext;
    private OnlinehearingRequest onlinehearingRequest;

    @Autowired
    public ApiSteps(TestContext testContext) {
        this.testContext = testContext;
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
                Onlinehearing onlinehearing = new Onlinehearing();
                onlinehearing.setCaseId(caseId);
                onlinehearing = onlinehearingService.retrieveOnlinehearingByCaseId(onlinehearing);
                onlinehearingPanelMemberRepository.deleteByOnlinehearing(onlinehearing);
                onlinehearingService.deleteByCaseId(caseId);
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
        Onlinehearing onlinehearing = testContext.getScenarioContext().getCurrentOnlinehearing();
        HttpGet request = new HttpGet(baseUrl + endpoint + "/" + onlinehearing.getOnlinehearingId().toString());
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
        CreateOnlinehearingResponse response = (CreateOnlinehearingResponse) JsonUtils.toObjectFromJson(responseString, CreateOnlinehearingResponse.class);
        assertEquals(response.getOnlinehearingId(), UUID.fromString(response.getOnlinehearingId()).toString());
    }

    @Given("^a standard online hearing is created$")
    public void aStandardOnlinehearingIsCreated() throws Throwable {
        String jsonBody = JsonUtils.getJsonInput("online_hearing/standard_online_hearing");

        OnlinehearingRequest onlinehearingRequest = (OnlinehearingRequest)JsonUtils.toObjectFromJson(jsonBody, OnlinehearingRequest.class);
        HttpEntity<String> request = new HttpEntity<>(jsonBody, header);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/continuous-online-hearings", HttpMethod.POST, request, String.class);
        String responseString = response.getBody();
        testContext.getScenarioContext().setCurrentOnlinehearing(onlinehearingRequest);
        testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);

        CreateOnlinehearingResponse newOnlinehearing = (CreateOnlinehearingResponse)JsonUtils.toObjectFromJson(responseString, CreateOnlinehearingResponse.class);
        testContext.getScenarioContext().getCurrentOnlinehearing().setOnlinehearingId(UUID.fromString(newOnlinehearing.getOnlinehearingId()));
    }

    @Given("^a standard online hearing$")
    public void aStandardOnlinehearing() throws IOException {
        onlinehearingRequest = (OnlinehearingRequest) JsonUtils.toObjectFromTestName("online_hearing/standard_online_hearing", OnlinehearingRequest.class);
    }

    @And("^the online hearing jurisdiction is ' \"([^\"]*)\" '$")
    public void theOnlinehearingJurisdictionIsSCSS(String jurisdictionName){
        onlinehearingRequest.setJurisdiction(jurisdictionName);
    }

    @And("^the post request is sent to create the online hearing$")
    public void thePostRequestIsSentToCreateTheOnlinehearing() throws IOException {

        String jsonBody = JsonUtils.toJson(onlinehearingRequest);
        HttpEntity<String> request = new HttpEntity<>(jsonBody, header);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/continuous-online-hearings", HttpMethod.POST, request, String.class);
        String responseString = response.getBody();
        testContext.getScenarioContext().setCurrentOnlinehearing(onlinehearingRequest);
        testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);

        CreateOnlinehearingResponse newOnlinehearing = (CreateOnlinehearingResponse)JsonUtils.toObjectFromJson(responseString, CreateOnlinehearingResponse.class);
        testContext.getScenarioContext().getCurrentOnlinehearing().setOnlinehearingId(UUID.fromString(newOnlinehearing.getOnlinehearingId()));

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
}