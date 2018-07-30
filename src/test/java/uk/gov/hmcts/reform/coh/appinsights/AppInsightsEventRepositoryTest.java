package uk.gov.hmcts.reform.coh.appinsights;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
public class AppInsightsEventRepositoryTest {

    @Mock
    private TelemetryClient telemetryClient;

    @InjectMocks
    private AppInsightsEventRepository eventRepository;

    @Test
    public void testAppInsightsEventRepository() {
        String key = "foo";
        Map<String, String> properties = new HashMap<>();
        properties.put(key, "bar");
        eventRepository.trackEvent(key, properties);

        Mockito.verify(eventRepository, Mockito.times(1)).trackEvent(key, properties);
    }
}
