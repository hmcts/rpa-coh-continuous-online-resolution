package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.schedule.notifiers.EventNotifierJob;

import java.util.UUID;

@ContextConfiguration
@SpringBootTest
public class DeadlineSteps extends BaseSteps {

    @Autowired
    private EventNotifierJob job;

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

    @When("^deadline extension is requested$")
    public void deadlineExtensionIsRequested() {
        job.execute();

        UUID hearingId = testContext.getScenarioContext().getCurrentOnlineHearing().getOnlineHearingId();
        String endpoint = "/continuous-online-hearings/" + hearingId + "/deadline-extensions";
        HttpEntity<String> request = new HttpEntity<>("");
        ResponseEntity<String> response =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, request, String.class);

        testContext.getHttpContext().setHttpResponseStatusCode(response.getStatusCodeValue());
    }
}
