package uk.gov.hmcts.reform.coh.functional.bdd.requests;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;

@Component
public class ConversationEndpointHandler extends AbstractRequestEndpoint {

    @Override
    public String getUrl(HttpMethod method, TestContext testContext) {
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();

        return baseUrl + CohUriBuilder.buildConversationsGet(onlineHearing.getOnlineHearingId());
    }

    @Override
    public String supports() {
        return CohEntityTypes.CONVERSATIONS.toString();
    }
}
