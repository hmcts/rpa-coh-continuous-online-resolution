package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BaseSteps {

    @Value("${base-urls.test-url}")
    String baseUrl;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    private ResponseEntity<String> response;

    private Map<String, String> endpoints = new HashMap<String, String>();

    private UUID onlineHearingId;

    @Before
    public void setUp() {
        endpoints.put("online-hearing", "/online-hearings");
    }

    @Given("^a standard online hearing$")
    /**
     * Creates a standard online hearing to be used for testing purposes
     */
    public void a_standard_online_hearing() throws IOException {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setExternalRef("StandardTestOnlineHearing");
        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(JsonUtils.toJson(onlineHearing), header);
        response = restTemplate.exchange(baseUrl + endpoints.get("online-hearing"), HttpMethod.POST, request, String.class);
        String json = response.getBody();
        onlineHearing = (OnlineHearing) JsonUtils.toObjectFromJson(json, OnlineHearing.class);
        this.onlineHearingId = onlineHearing.getOnlineHearingId();
    }

}
