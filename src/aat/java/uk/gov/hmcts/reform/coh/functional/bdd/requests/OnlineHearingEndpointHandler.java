package uk.gov.hmcts.reform.coh.functional.bdd.requests;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;

@Component
public class OnlineHearingEndpointHandler extends AbstractRequestEndpoint {

    @Override
    public String getUrl(HttpMethod method, TestContext testContext) {
        OnlineHearing onlineHearing = null;

        try {
            testContext.getScenarioContext().getCurrentOnlineHearing();
        } catch (Exception e) {
        }

        String url = null;
        if (HttpMethod.POST.equals(method)) {
            url = CohUriBuilder.buildOnlineHearingPost();
        } else if (HttpMethod.GET.equals(method)) {
            url = CohUriBuilder.buildOnlineHearingGet(onlineHearing.getOnlineHearingId());
        } else if (HttpMethod.PUT.equals(method)) {
            url = CohUriBuilder.buildOnlineHearingGet(onlineHearing.getOnlineHearingId());
        } else {
            throw new NotImplementedException("Request Handler not implemented for http method " + method);
        }

        return baseUrl + url;
    }

    @Override
    public String supports() {
        return CohEntityTypes.ONLINE_HEARING.getString();
    }
}
