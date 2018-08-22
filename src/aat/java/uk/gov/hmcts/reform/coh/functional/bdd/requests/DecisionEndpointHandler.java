package uk.gov.hmcts.reform.coh.functional.bdd.requests;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;

@Component
public class DecisionEndpointHandler extends AbstractRequestEndpoint {

    @Override
    public String getUrl(HttpMethod method, TestContext testContext) {

        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        String url;
        if (HttpMethod.POST.equals(method) || HttpMethod.GET.equals(method)) {
            url = CohUriBuilder.buildDecisionGet(onlineHearing.getOnlineHearingId());
        } else {
            throw new NotImplementedException(String.format("Http method %s not implemented for %s", method, getClass()));
        }

        return baseUrl + url;
    }

    @Override
    public String supports() {
        return CohEntityTypes.DECISION.getString();
    }
}
