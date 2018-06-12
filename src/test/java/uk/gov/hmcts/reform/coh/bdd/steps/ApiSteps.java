package uk.gov.hmcts.reform.coh.bdd.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import org.apache.http.entity.StringEntity;

import static junit.framework.TestCase.assertTrue;

public class ApiSteps {

    private JSONObject json;

    private HttpPost request;
    private HttpResponse response;
    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();

//    @Before
//    public void setup(){
//        // figure out how to append urls fragments to a base url
//
//    }

    // maybe collapse the below steps into a single step
    @Given("^SSCS prepare a json request$")
    public void sscs_prepare_a_json_request() {
        request = new HttpPost("localhost:8080/online-hearings/create");
        request.addHeader("content-type", "application/json");
        System.out.println("HERE");
        json = new JSONObject();
    }

    @Given("^set the 'externalRef' field to ' \"([^\"]*)\": '$")
    public void setTheExternalRefFieldTo(String externalRef) throws Throwable {
        json.put("externalRef", externalRef);
    }

    @When("^a post request is sent to /online-hearings/create$")
    public void a_post_request_is_sent_to_online_hearings_create() throws Throwable {
        StringEntity params = new StringEntity(json.toString());
        request.setEntity(params);
        response = httpClient.execute(request);
    }

    @Then("^the response contains the following text '\"([^\"]*)\": '$")
    public void the_response_contains_the_following_text(String text) {
        String message = response.toString();
        System.out.println(message);
        assertTrue(message.contains(text));
    }

    // Remove added entities from the database in cleanup

}
