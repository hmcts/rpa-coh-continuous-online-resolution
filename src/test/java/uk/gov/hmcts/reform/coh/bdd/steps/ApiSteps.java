package uk.gov.hmcts.reform.coh.bdd.steps;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import org.apache.http.entity.StringEntity;
import sun.net.www.http.HttpClient;

public class ApiSteps {

    private JSONObject json;
    private StringEntity params;

    private HttpURLConnection http;

    @Before
    public void setup(){
        try {
            URL url = new URL("localhost:8080/online-hearings/create");
            URLConnection connection = url.openConnection();
            http = (HttpURLConnection) connection;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Given("^'SSCS' prepare a json request$")
    public void sscs_prepare_a_json_request() throws Throwable {
        json = new JSONObject();
    }

    @And("^set the 'externalRef' field to ' \"([^\"]*)\": '$")
    public void setTheExternalRefFieldTo(String externalRef) throws Throwable {
        json.put("externalRef", externalRef);
    }

    @When("^a post request is sent to '/online-hearings/create'$")
    public void a_post_request_is_sent_to_online_hearings_create() throws Throwable {
        //http.setRequestMethod("POST");
        params = new StringEntity(json.toString());
        http.setDoOutput(true); // implicitly sets the request method to POST
        http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(params);
        }
    }

    @Then("^the response contains the following text '\"([^\"]*)\": '$")
    public void the_response_contains_the_following_text(String text) throws Throwable {

    }

}
