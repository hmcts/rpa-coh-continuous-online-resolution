package uk.gov.hmcts.reform.coh.idam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.idam.client.LoggingIdamClient;
import uk.gov.hmcts.reform.coh.idam.client.RestTemplateIdamClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;

@Component
@Scope("singleton")
public class IdamAuthentication implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(IdamAuthentication.class);

    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    @Value("${base-urls.idam-url}")
    protected String url;

    @Value("${base-urls.idam-user-role}")
    protected String role;

    @Value("${base-urls.idam-user-email}")
    private String email;

    private long delay = 25;

    private TimeUnit unit = TimeUnit.SECONDS;

    private IdamClient client;

    private String token;

    @PostConstruct
    public void init() {
        client = new LoggingIdamClient(new RestTemplateIdamClient(url));
        scheduledExecutor.scheduleWithFixedDelay(this::refreshToken, 0, delay, unit);
    }

    private void refreshToken() {
        Integer userId = client.findUserByEmail(email);
        if (userId == 0) {
            client.createAccount(email, role);
            userId = client.findUserByEmail(email);
        }
        token = client.lease(userId, role);
        if (token != null && !"".equals(token)) {
            log.info("Token refreshed");
        } else {
            log.warn("Empty token!");
        }
    }

    public String getToken() {
        return token;
    }

    @Override
    public void close() throws Exception {
        scheduledExecutor.shutdown();
    }
}
