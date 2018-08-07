package uk.gov.hmcts.reform.coh.schedule.notifiers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.controller.exceptions.IdamHeaderInterceptor;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;

import java.io.IOException;

@Component
@Qualifier("BasicJsonNotificationForwarder")
public class BasicJsonNotificationForwarder implements NotificationForwarder<NotificationRequest> {

    private static final Logger log = LoggerFactory.getLogger(BasicJsonNotificationForwarder.class);

    private static final String PLACEHOLDER_HOST = "${base-urls.test-url}";

    private static final ObjectMapper mapper = new ObjectMapper();

    private static HttpHeaders URL_ENCODED_HEADER;

    private static String IDAM_AUTHORISATION_TOKEN = "test_idam_service";
    private static String IDAM_SERVICE_TOKEN = "test_idam_service";
    static {
        URL_ENCODED_HEADER = new HttpHeaders();
        URL_ENCODED_HEADER.add("Content-Type", "application/json");
        URL_ENCODED_HEADER.add(IdamHeaderInterceptor.IDAM_AUTHORIZATION, IDAM_AUTHORISATION_TOKEN);
        URL_ENCODED_HEADER.add(IdamHeaderInterceptor.IDAM_SERVICE_AUTHORIZATION, IDAM_SERVICE_TOKEN);
    }

    @Value("${base-urls.test-url}")
    String baseUrl;

    @Override
    public ResponseEntity sendEndpoint(SessionEventForwardingRegister register, NotificationRequest notificationRequest) throws NotificationException {

        String endpoint = refactorEndpoint(register.getForwardingEndpoint());

        ResponseEntity response = null;
        try {
            log.info(String.format("Sending request to %s", endpoint));
            RestTemplate restTemplate = getRestTemplate();
            HttpEntity<String> request = new HttpEntity<>( mapper.writeValueAsString(notificationRequest), URL_ENCODED_HEADER);
            response = restTemplate.exchange(endpoint, HttpMethod.POST, request, String.class);
            log.info(String.format("Endpoint responded with %s", response.getStatusCodeValue()));
        } catch (IOException ioe) {
            throw new NotificationException(ioe.getMessage());
        }  catch (HttpClientErrorException hcee) {
            throw new NotificationException("HTTP error. Endpoint responded with " + hcee.getRawStatusCode() + " and response body " + hcee.getResponseBodyAsString());
        } catch (Exception e){
            throw new NotificationException(e.getMessage());
        }

        return response;
    }

    public String refactorEndpoint(String endpoint) {
        if (endpoint.contains(PLACEHOLDER_HOST)) {
            endpoint = endpoint.replace("${base-urls.test-url}", getBaseUrl());
        }

        return endpoint;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}