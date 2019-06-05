package uk.gov.hmcts.reform.coh.idam;

import io.restassured.RestAssured;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class IdamHelper {

    private static final Logger log = LoggerFactory.getLogger(IdamHelper.class);

    private static final String USERNAME = "coh2testytesttest@test.net";
    private static final String PASSWORD = "4590fgvhbfgbDdffm3lk4j";

    private final String idamUrl;
    private final String client;
    private final String secret;
    private final String redirect;

    public IdamHelper(@Value("${base-urls.idam-url}") String idamUrl,
                      @Value("${test.client_name}") String client,
                      @Value("${test.client_secret}") String secret,
                      @Value("${test.client_redirect}") String redirect) {
        this.idamUrl = idamUrl;
        this.client = client;
        this.secret = secret;
        this.redirect = redirect;
    }

    public String getIdamToken() {
        for (int i = 0; i < 10; i++) {
            try {
                return getIdamTokenFromApi();
            } catch (RuntimeException e) {
                log.info("Failed to get IDAM token, trying again");
            }
        }

        throw new IdamTokenException("Failed to get IDAM token, aborting");
    }

    private String getIdamTokenFromApi() {
        createUser();

        String code = getCode();
        String token = getToken(code);

        log.debug(String.format("Generated USER token: %s", token));

        return "Bearer " + token;
    }

    private void createUser() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", USERNAME);
        jsonObject.put("password", PASSWORD);
        jsonObject.put("forename", "test");
        jsonObject.put("surname", "test");
        JSONArray roles = new JSONArray();
        JSONObject role = new JSONObject();
        role.put("code", "citizen");
        roles.put(0, role);
        jsonObject.put("roles", roles);

        RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(jsonObject.toString())
            .post(idamUrl + "/testing-support/accounts");
    }

    private String getCode() {
        String credentials = USERNAME + ":" + PASSWORD;
        String authHeader = Base64.getEncoder().encodeToString(credentials.getBytes());

        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header("Authorization", "Basic " + authHeader)
            .formParam("redirect_uri", redirect)
            .formParam("client_id", client)
            .formParam("response_type", "code")
            .post(idamUrl + "/oauth2/authorize")
            .jsonPath()
            .get("code");
    }

    private String getToken(String code) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .formParam("code", code)
            .formParam("grant_type", "authorization_code")
            .formParam("redirect_uri", redirect)
            .formParam("client_id", client)
            .formParam("client_secret", secret)
            .post(idamUrl + "/oauth2/token")
            .jsonPath()
            .getString("access_token");
    }

    public class IdamTokenException extends RuntimeException {
        public IdamTokenException(String message) {
            super(message);
        }
    }
}
