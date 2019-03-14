package uk.gov.hmcts.reform.coh.idam;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class StrategicIdamAuthentication extends AbstractIdamAuthentication {

    @Value("${oauth2.client.id}")
    private String clientId;

    @Value("${oauth2.client.secret}")
    private String clientSecret;

    @Value("${oauth2.client.redirect}")
    private String redirectUrl;

    @Override
    protected String newToken() {
        String password = DigestUtils.sha1Hex(email);
        client.createAccount(email, role, password);
        String code = client.authenticate(email, password, "code", clientId, redirectUrl);
        return client.exchangeCode(code, "authorization_code", clientId, clientSecret, redirectUrl);
    }
}
