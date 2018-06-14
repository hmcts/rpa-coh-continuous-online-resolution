package uk.gov.hmcts.reform.coh.bdd.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerRequest;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class AnswerSteps {

    private String baseUrl = "http://localhost:8080";

    private TestRestTemplate restTemplate = new TestRestTemplate();

    private ResponseEntity<String> response;

    private AnswerRequest request = new AnswerRequest();

    private String endpoint;

    @Given("^a valid answer$")
    public void a_valid_answer() {
        request.setAnswerText("foo");
    }

    @Given("^the endpoint is '(.*)'$")
    public void the_endpoint_is(String endpoint) {
        this.endpoint = endpoint;
    }

    @When("^a (.*) request is sent$")
    public void send_request(String type) throws IOException {

        String json = convertToJson(request);

        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type","application/json");

        if ("GET".equalsIgnoreCase(type)) {
            response = restTemplate.getForEntity(baseUrl + endpoint, String.class);
        } else {
            HttpEntity<String> request = new HttpEntity<>(json, header);
            response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.POST, request, String.class);
        }
    }

    @Then("^the response code is (\\d+)$")
    public void the_response_code_is(int responseCode) throws Throwable {
        assertEquals("Response status code", responseCode, response.getStatusCode().value());
    }

    private String convertToJson(Object obj) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(obj);
    }
}