package uk.gov.hmcts.reform.coh.functional.bdd.runner;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

@RunWith(Cucumber.class)
@CucumberOptions(features = {"src/aat/resources/cucumber/online-hearing.feature"},
        format = {"pretty", "html:target/reports/cucumber/html",
                "json:target/cucumber.json", "usage:target/usage.jsonx", "junit:target/junit.xml"},
        glue = {"uk/gov/hmcts/reform/coh/functional/bdd/steps"})
public class ContinuousOnlineHearingTests {}