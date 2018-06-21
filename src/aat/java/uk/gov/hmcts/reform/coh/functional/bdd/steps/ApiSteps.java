package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@ContextConfiguration
@SpringBootTest
public class ApiSteps {

    @Autowired
    private OnlineHearingService onlineHearingService;

    private JSONObject json;
    private String baseUrl;

    private HttpResponse response;
    private String responseString;
    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    private Set<String> newObjects;
    private Set<OnlineHearing> newOnlineHearings;

    @Before
    public void setup(){
        baseUrl = "http://localhost:8080/online-hearings";
        newObjects = new HashSet<String>();
        newOnlineHearings = new HashSet<OnlineHearing>();
    }

    @After
    public void cleanUp() {
//        for (String object : newObjects) {
//            OnlineHearing onlineHearing = new OnlineHearing();
//            onlineHearing.setExternalRef(object);
//            onlineHearingService.deleteOnlineHearingByExternalRef(onlineHearing);
//        }

        for (OnlineHearing oh : newOnlineHearings) {
            Optional<OnlineHearing> opt = onlineHearingService.retrieveOnlineHearing(oh);
            if (opt.isPresent()) {
                onlineHearingService.deleteOnlineHearing(oh);
            }
        }
    }

    @Given("^SSCS prepare a json request with the ' \"([^\"]*)\"' field set to ' \"([^\"]*)\" '$")
    public void sscs_prepare_a_json_request_with_the_field_set_to(String fieldName, String fieldInput) throws Throwable {
        json = new JSONObject();
        json.put(fieldName, fieldInput);
        newObjects.add(fieldInput);
    }

    @When("^a get request is sent to ' \"([^\"]*)\"'$")
    public void a_get_request_is_sent_to(String endpoint) throws Throwable {
        HttpGet request = new HttpGet(baseUrl + endpoint);
        request.addHeader("content-type", "application/json");

        response = httpClient.execute(request);
    }

    @When("^a post request is sent to ' \"([^\"]*)\"'$")
    public void a_post_request_is_sent_to(String endpoint) throws Throwable {
        HttpPost request = new HttpPost(baseUrl + endpoint);
        request.addHeader("content-type", "application/json");
        StringEntity params = new StringEntity(json.toString());
        request.setEntity(params);
        response = httpClient.execute(request);
        responseString = new BasicResponseHandler().handleResponse(response);
        OnlineHearing oh = (OnlineHearing) JsonUtils.toObjectFromJson(responseString, OnlineHearing.class);
        newOnlineHearings.add(oh);
    }

    @Then("^the client receives a (\\d+) status code$")
    public void the_client_receives_a_status_code(final int expectedStatus) throws IOException {
        int currentStatusCode = response.getStatusLine().getStatusCode();
        assertEquals(expectedStatus, currentStatusCode);
        assertEquals("Status code is incorrect : " +
                response.getEntity().getContent().toString(), expectedStatus, currentStatusCode);
    }

    @Then("^the response contains the following text '\"([^\"]*)\" '$")
    public void the_response_contains_the_following_text(String text) {
        assertTrue(responseString.contains(text));
    }

}
