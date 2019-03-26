package uk.gov.hmcts.reform.coh.functional.rest;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.Application;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingRequest;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohEndpointFactory;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohEndpointHandler;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohEntityTypes;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestUtil;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, properties = "SpringBootTest")
@TestPropertySource(locations = "classpath:application.yaml")
public class TemplateScenarios {

    @Test
    public void testGetTemplates() throws Exception {
        TestUtil testUtil = new TestUtil();
        OnlineHearingRequest onlineHearingRequest = JsonUtils
                .toObjectFromTestName("online_hearing/standard_online_hearing", OnlineHearingRequest.class);
        CohEndpointHandler endpoint = CohEndpointFactory.getRequestEndpoint(CohEntityTypes.ONLINE_HEARING.getString());

        Response response = testUtil
                .authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(onlineHearingRequest)
                .request("POST",endpoint.getUrl(HttpMethod.POST, null));

        JsonPath body = response.getBody().jsonPath();
        Assert.assertEquals(201, response.getStatusCode());
    }
}
