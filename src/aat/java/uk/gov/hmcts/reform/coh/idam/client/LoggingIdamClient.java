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
    public void createAccount(String email, String role, String password) {
        subject.createAccount(email, role, password);
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

    @Override
    public String authenticate(String user, String password, String responseType, String clientId, String redirectUri) {
        String code = subject.authenticate(user, password, responseType, clientId, redirectUri);
        log.info("Generated code for {} from {}", user, clientId);
        return code;
    }

    @Override
    public String exchangeCode(
        String code,
        String grantType,
        String clientId,
        String clientSecret,
        String redirectUri
    ) {
        String token = subject.exchangeCode(code, grantType, clientId, clientSecret, redirectUri);
        log.info("Generated token for {}: {}", clientId, token);
        return token;
    }
}
