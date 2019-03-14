package uk.gov.hmcts.reform.coh.idam;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope("singleton")
@ConditionalOnMissingBean(name = "strategicIdamAuthentication")
public class TacticalIdamAuthentication extends AbstractIdamAuthentication {

    @Override
    protected String getNewToken() {
        Integer userId = client.findUserByEmail(email);
        if (userId == 0) {
            client.createAccount(email, UUID.randomUUID().toString(), role);
            userId = client.findUserByEmail(email);
        }
        return client.lease(userId, role);
    }
}
