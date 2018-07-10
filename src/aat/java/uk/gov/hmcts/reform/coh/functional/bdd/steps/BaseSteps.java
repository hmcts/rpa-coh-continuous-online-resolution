package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.coh.controller.onlineHearing.CreateOnlineHearingResponse;
import uk.gov.hmcts.reform.coh.controller.onlineHearing.OnlineHearingResponse;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;

import java.util.UUID;

public class BaseSteps {

    @Value("${base-urls.test-url}")
    String baseUrl;

    OnlineHearing createOnlineHearingFromResponse(CreateOnlineHearingResponse response) {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(UUID.fromString(response.getOnlineHearingId()));

        return onlineHearing;
    }

    OnlineHearing createOnlineHearingFromResponse(OnlineHearingResponse response) {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(response.getOnlineHearingId());

        return onlineHearing;
    }

}
