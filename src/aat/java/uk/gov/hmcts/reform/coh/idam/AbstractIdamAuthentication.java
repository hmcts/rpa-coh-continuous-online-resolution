package uk.gov.hmcts.reform.coh.idam;

import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.coh.idam.client.LoggingIdamClient;
import uk.gov.hmcts.reform.coh.idam.client.RestTemplateIdamClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;

public abstract class AbstractIdamAuthentication implements AutoCloseable {

    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    @Value("${base-urls.idam-url}")
    protected String url;

    @Value("${base-urls.idam-user-role}")
    protected String role;

    @Value("${base-urls.idam-user-email}")
    protected String email;
    protected IdamClient client;
    private long delay = 25;
    private TimeUnit unit = TimeUnit.SECONDS;
    private String token;

    @PostConstruct
    public void init() {
        client = new LoggingIdamClient(new RestTemplateIdamClient(url));
        scheduledExecutor.scheduleWithFixedDelay(this::refreshToken, 0, delay, unit);
    }

    private void refreshToken() {
        token = getNewToken();
    }

    abstract protected String getNewToken();

    public String getToken() {
        return token;
    }

    @Override
    public void close() throws Exception {
        scheduledExecutor.shutdown();
    }
}
