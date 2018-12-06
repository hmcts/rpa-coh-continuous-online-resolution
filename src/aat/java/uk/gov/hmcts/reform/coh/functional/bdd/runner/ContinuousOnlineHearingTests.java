package uk.gov.hmcts.reform.coh.functional.bdd.runner;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

@RunWith(Cucumber.class)
@CucumberOptions(features = {"src/aat/resources/cucumber"},
        format = {"pretty", "html:reports/cucumber/html",
                "json:reports/cucumber.json", "usage:reports/usage.jsonx", "junit:target/junit.xml"},
        glue = {"uk/gov/hmcts/reform/coh/functional/bdd/steps/"})
@ActiveProfiles({"cucumber"})
public class ContinuousOnlineHearingTests {}