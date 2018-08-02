package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.After;
import cucumber.api.java.Before;
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
import org.springframework.web.client.ResponseErrorHandler;
import uk.gov.hmcts.reform.coh.controller.question.AllQuestionsResponse;
import uk.gov.hmcts.reform.coh.controller.question.QuestionResponse;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.schedule.notifiers.EventNotifierJob;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

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

    @After
    public void cleanup() {
        super.cleanup();
    }

    @When("^deadline extension is requested(?: again)?$")
    public void deadlineExtensionIsRequested() {
        job.execute();

        UUID hearingId = testContext.getScenarioContext().getCurrentOnlineHearing().getOnlineHearingId();
        String endpoint = "/continuous-online-hearings/" + hearingId + "/questions-deadline-extension";
        HttpEntity<String> request = new HttpEntity<>("");

        setupRestTemplate();

        ResponseEntity<String> response =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, request, String.class);

        restoreRestTemplate();

        testContext.getHttpContext().setHttpResponseStatusCode(response.getStatusCodeValue());
    }

    private void setupRestTemplate() {
        oldErrorHandler = restTemplate.getErrorHandler();

        // valid response codes from the endpoint are not errors
        restTemplate.setErrorHandler(new ResponseErrorHandler() {

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
        restTemplate.setErrorHandler(oldErrorHandler);
    }

    @Then("^question states are (.*)$")
    public void questionsDeadlinesHaveBeenSuccessfullyExtended(String questionExpectedState) throws Throwable {
        // load questions into scenario context
        questionSteps.get_all_questions_for_a_online_hearing();

        String rawJson = testContext.getHttpContext().getRawResponseString();
        AllQuestionsResponse allQuestionsResponse = JsonUtils.toObjectFromJson(rawJson, AllQuestionsResponse.class);
        List<QuestionResponse> questionResponses = allQuestionsResponse.getQuestions();

        Calendar expectedExpiryDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        expectedExpiryDate.add(Calendar.DAY_OF_YEAR, 7 + 7); // deadline for answer + extension
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
}
