package uk.gov.hmcts.reform.coh.smoke;

import io.restassured.RestAssured;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

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
    public void testAppIsUp() {
        RestAssured.useRelaxedHTTPSValidation();

        RestAssured.given()
                .request("GET", baseUrl + "/health")
                .then()
                .statusCode(200);
    }
}
