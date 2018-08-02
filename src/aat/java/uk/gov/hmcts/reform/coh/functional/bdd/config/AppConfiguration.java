package uk.gov.hmcts.reform.coh.functional.bdd.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestTrustManager;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@TestConfiguration
public class AppConfiguration {

    @Bean
    public RestTemplate restTemplate() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return new RestTemplate(TestTrustManager.getInstance().getTestRequestFactory());
    }
}
