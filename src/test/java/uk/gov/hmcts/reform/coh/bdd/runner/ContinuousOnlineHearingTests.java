package uk.gov.hmcts.reform.coh.bdd.runner;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = {"src/test/resources/cucumber"},
        format = {"pretty", "html:target/reports/cucumber/html",
                "json:target/cucumber.json", "usage:target/usage.jsonx", "junit:target/junit.xml"},
        glue = {"uk/gov/hmcts/reform/coh/bdd/steps"})

public class ContinuousOnlineHearingTests {}