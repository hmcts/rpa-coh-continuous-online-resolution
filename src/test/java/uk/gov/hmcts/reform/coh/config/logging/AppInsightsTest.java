package uk.gov.hmcts.reform.coh.config.logging;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class AppInsightsTest {

    private AppInsights appInsights;

    @Test
    public void testBeanCompiles() {
        TelemetryClient telemetryClient = new TelemetryClient();
        telemetryClient.getContext().setInstrumentationKey("foo");
        appInsights = new AppInsights(telemetryClient);
        assertNotNull(appInsights);
    }
}