package uk.gov.hmcts.reform.coh.functional.bdd.requests;

import org.springframework.beans.factory.annotation.Value;

abstract public class AbstractRequestEndpoint implements CohEndpointHandler {

    @Value("${base-urls.test-url}")
    protected String baseUrl;
}
