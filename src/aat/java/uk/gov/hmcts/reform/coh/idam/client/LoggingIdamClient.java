package uk.gov.hmcts.reform.coh.idam.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.coh.idam.IdamClient;

public class LoggingIdamClient implements IdamClient {

    private static final Logger log = LoggerFactory.getLogger(LoggingIdamClient.class);
    private final IdamClient subject;

    public LoggingIdamClient(IdamClient subject) {
        this.subject = subject;
    }

    @Override
    public void createAccount(String email, String role) {
        subject.createAccount(email, role);
        log.info("Created account for {} as {}", email, role);
    }

    @Override
    public Integer findUserByEmail(String email) {
        Integer userId = subject.findUserByEmail(email);
        log.info("Resolved {} as {}", email, userId);
        return userId;
    }

    @Override
    public String lease(Integer userId, String role) {
        String token = subject.lease(userId, role);
        log.info("Generated token for userId = {}: {}", userId, token);
        return token;
    }
}
