package uk.gov.hmcts.reform.coh.functional.bdd.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestContext {

    @Autowired
    private ScenarioContext scenarioContext;

    @Autowired
    private HttpContext httpContext;

    public ScenarioContext getScenarioContext() {
        return scenarioContext;
    }

    public HttpContext getHttpContext() {
        return httpContext;
    }
}