package uk.gov.hmcts.reform.coh.idam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;

@Component
@Scope("singleton")
public class IdamAuthentication implements AutoCloseable {

    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    @Value("${base-urls.idam-url}")
    protected String url;

    @Value("${base-urls.idam-user-role}")
    protected String role;

    @Value("${base-urls.idam-user-email}")
    private String email;

    @Autowired
    private IdamHelper idamHelper;

    private long delay = 600;

    private TimeUnit unit = TimeUnit.SECONDS;

    private String token;

    @PostConstruct
    public void init() {
        scheduledExecutor.scheduleWithFixedDelay(this::refreshToken, 0, delay, unit);
    }

    private void refreshToken() {
        token = idamHelper.getIdamToken();
    }

    public String getToken() {
        return idamHelper.getIdamToken();
    }

    @Override
    public void close() {
        scheduledExecutor.shutdown();
    }
}
