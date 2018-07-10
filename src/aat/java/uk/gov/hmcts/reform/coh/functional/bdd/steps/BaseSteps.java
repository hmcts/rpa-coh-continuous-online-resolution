package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.CreateOnlinehearingResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlinehearingResponse;
import uk.gov.hmcts.reform.coh.domain.Onlinehearing;

import java.util.UUID;

public class BaseSteps {

    @Value("${base-urls.test-url}")
    String baseUrl;

    Onlinehearing createOnlinehearingFromResponse(CreateOnlinehearingResponse response) {
        Onlinehearing onlinehearing = new Onlinehearing();
        onlinehearing.setOnlinehearingId(UUID.fromString(response.getOnlinehearingId()));

        return onlinehearing;
    }

    Onlinehearing createOnlinehearingFromResponse(OnlinehearingResponse response) {
        Onlinehearing onlinehearing = new Onlinehearing();
        onlinehearing.setOnlinehearingId(response.getOnlinehearingId());

        return onlinehearing;
    }

}
