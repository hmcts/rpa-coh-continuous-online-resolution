package uk.gov.hmcts.reform.coh.functional.bdd.utils;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.json.JSONException;

public class TestUtil {

    private final String idamAuth;
    private final String s2sAuth;

    public TestUtil() throws JSONException {
        IdamHelper idamHelper = new IdamHelper(
            Env.getIdamUrl(),
            Env.getOAuthClient(),
            Env.getOAuthSecret(),
            Env.getOAuthRedirect()
        );

        S2sHelper s2sHelper = new S2sHelper(
            Env.getS2sUrl(),
            Env.getS2sSecret(),
            Env.getS2sMicroservice()
        );

        idamAuth = idamHelper.getIdamToken();
        s2sAuth = s2sHelper.getS2sToken();

        RestAssured.useRelaxedHTTPSValidation();
    }

    public String getIdamAuth() {
        return idamAuth;
    }

    public String getS2sAuth() {
        return s2sAuth;
    }

    public RequestSpecification authRequest() {
        return RestAssured
            .given()
            .header("Authorization", idamAuth)
            .header("ServiceAuthorization", s2sAuth);
    }
}
