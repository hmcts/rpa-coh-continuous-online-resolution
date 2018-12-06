package uk.gov.hmcts.reform.coh.smoke;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestTrustManager;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ContextConfiguration
@SpringBootTest
@ActiveProfiles("cucumber")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class SmokeTest {

    private RestTemplate restTemplate;

    @Value("${base-urls.test-url}")
    protected String baseUrl;

    @Test
    public void testAppIsUp() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        restTemplate = new RestTemplate(TestTrustManager.getInstance().getTestRequestFactory());
        ResponseEntity<Health> response =restTemplate.getForEntity(baseUrl + "/health", Health.class);
        Health health = response.getBody();

        assertEquals("UP", health.getStatus());
    }
}
