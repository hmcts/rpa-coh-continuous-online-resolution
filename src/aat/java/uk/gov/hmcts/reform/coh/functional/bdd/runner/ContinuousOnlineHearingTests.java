package uk.gov.hmcts.reform.coh.functional.bdd.runner;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

@RunWith(Cucumber.class)
@CucumberOptions(features = {"src/aat/resources/cucumber"},
        format = {"pretty", "html:build/reports/cucumber/html",
                "json:build/reports/cucumber/cucumber.json", "usage:build/usage.jsonx", "junit:build/junit.xml"},
        glue = {"uk/gov/hmcts/reform/coh/functional/bdd/steps/"})
@ActiveProfiles({"cucumber"})
public class ContinuousOnlineHearingTests {}