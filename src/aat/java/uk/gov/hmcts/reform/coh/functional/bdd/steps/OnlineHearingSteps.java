package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingsResponse;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class OnlineHearingSteps extends BaseSteps {

    private TestContext testContext;

    @Autowired
    public OnlineHearingSteps(TestContext testContext){
        this.testContext = testContext;
    }

    @Then("^the response contains (\\d) online hearings$")
    public void the_response_contains_no_online_hearings(int count) throws IOException {
        OnlineHearingsResponse response = (OnlineHearingsResponse) JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), OnlineHearingsResponse.class);
        assertEquals(count, response.getOnlineHearingResponses().size());
    }

    @Then("^the response contains online hearing with case '(.*)'$")
    public void the_response_contains_online_hearing_with_case(String caseId) throws IOException {
        OnlineHearingsResponse response = (OnlineHearingsResponse) JsonUtils.toObjectFromJson(testContext.getHttpContext().getRawResponseString(), OnlineHearingsResponse.class);
        assertTrue(response.getOnlineHearingResponses().stream().anyMatch(o -> caseId.equalsIgnoreCase(o.getCaseId())));
    }
}
