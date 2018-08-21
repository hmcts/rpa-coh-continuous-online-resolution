package uk.gov.hmcts.reform.coh.functional.bdd.requests;

import org.springframework.http.HttpMethod;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;

public interface CohRequestEndpoint {

    String getUrl(HttpMethod method, TestContext testContext);

    String supports();
}
