package uk.gov.hmcts.reform.coh.functional.bdd.requests;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.domain.DecisionReply;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;

@Component
public class DecisionReplyEndpointHandler extends AbstractRequestEndpoint {

    @Override
    public String getUrl(HttpMethod method, TestContext testContext) {

        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        String url;
        if (HttpMethod.POST.equals(method)) {
            url = CohUriBuilder.buildDecisionReplyPost(onlineHearing.getOnlineHearingId());
        } else if (HttpMethod.GET.equals(method)) {
            DecisionReply decisionReply = testContext.getScenarioContext().getDecisionReplies().get(0);
            url = CohUriBuilder.buildDecisionReplyGet(onlineHearing.getOnlineHearingId(), decisionReply.getId());
        } else {
            throw new NotImplementedException(String.format("Http method %s not implemented for %s", method, getClass()));
        }

        return baseUrl + url;
    }

    @Override
    public String supports() {
        return CohEntityTypes.DECISION_REPLY.toString();
    }
}
