package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;
import uk.gov.hmcts.reform.coh.controller.question.AllQuestionsResponse;
import uk.gov.hmcts.reform.coh.controller.question.QuestionResponse;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.schedule.notifiers.EventNotifierJob;
import uk.gov.hmcts.reform.coh.service.utils.ExpiryCalendar;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.*;

import static org.junit.Assert.assertEquals;

@ContextConfiguration
@SpringBootTest
public class DeadlineSteps extends BaseSteps {

    @Autowired
    private EventNotifierJob job;

    @Autowired
    private QuestionSteps questionSteps;

    private ResponseErrorHandler oldErrorHandler;

    @Autowired
    public DeadlineSteps(TestContext testContext) {
        super(testContext);
    }

    @Before
    public void setup() throws Exception {
        super.setup();
    }

    @When("^deadline extension is requested(?: again)?$")
    public void deadlineExtensionIsRequested() throws Exception {
        job.execute();

        UUID hearingId = testContext.getScenarioContext().getCurrentOnlineHearing().getOnlineHearingId();
        String endpoint = "/continuous-online-hearings/" + hearingId + "/questions-deadline-extension";
        HttpEntity<String> request = new HttpEntity<>("", header);

        setupRestTemplate();

        try {
            ResponseEntity<String> response =
                    getRestTemplate().exchange(baseUrl + endpoint, HttpMethod.PUT, request, String.class);
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(hcee);
        }

        restoreRestTemplate();

    }

    private void setupRestTemplate() {
        oldErrorHandler = getRestTemplate().getErrorHandler();

        // valid response codes from the endpoint are not errors
        getRestTemplate().setErrorHandler(new ResponseErrorHandler() {

            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return !(response.getRawStatusCode() == 200
                    || response.getRawStatusCode() == 404
                    || response.getRawStatusCode() == 424
                    || response.getRawStatusCode() == 500)
                    && oldErrorHandler.hasError(response);
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                oldErrorHandler.handleError(response);
            }
        });
    }

    private void restoreRestTemplate() {
        getRestTemplate().setErrorHandler(oldErrorHandler);
    }

    @Then("^question states are (.*)$")
    public void questionsDeadlinesHaveBeenSuccessfullyExtended(String questionExpectedState) throws Throwable {
        // load questions into scenario context
        questionSteps.getAllQuestionsForOnlineHearing();

        List<QuestionResponse> questionResponses = getAllQuestionsResponse().getQuestions();

        Calendar expectedExpiryDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        expectedExpiryDate.add(Calendar.DAY_OF_YEAR, ExpiryCalendar.getInstance().getDeadlineExtensionDays() *2 ); // deadline for answer + extension
        expectedExpiryDate.set(Calendar.HOUR_OF_DAY, 23);
        expectedExpiryDate.set(Calendar.MINUTE, 59);
        expectedExpiryDate.set(Calendar.SECOND, 59);

        for (QuestionResponse questionResponse : questionResponses) {
            assertEquals(
                DateUtils.truncate(expectedExpiryDate.getTime(), Calendar.SECOND),
                DateUtils.truncate(Date.from(Instant.parse(questionResponse.getDeadlineExpiryDate())), Calendar.SECOND)
            );

            assertEquals(
                questionExpectedState,
                questionResponse.getCurrentState().getName()
            );
        }
    }

    @And("^the response message is '(.*)'$")
    public void theResponseMessageIsr(String message) {
        assertEquals(message, testContext.getHttpContext().getRawResponseString());
    }

    @And("^question deadline extendion count is (\\d+)$")
    public void questionDeadlineExtendionCountIs(int count) throws Throwable {
        getAllQuestionsResponse().getQuestions().forEach(q -> assertEquals(count, Integer.parseInt(q.getDeadlineExtCount())));
    }

    private AllQuestionsResponse getAllQuestionsResponse() throws Exception {
        String rawJson = testContext.getHttpContext().getRawResponseString();
        return JsonUtils.toObjectFromJson(rawJson, AllQuestionsResponse.class);
    }
}
