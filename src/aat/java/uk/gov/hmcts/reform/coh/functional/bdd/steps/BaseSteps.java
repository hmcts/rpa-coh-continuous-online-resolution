package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import org.springframework.beans.factory.annotation.Value;

public class BaseSteps {

    @Value("${base-urls.test-url}")
    String baseUrl;

}
