package uk.gov.hmcts.reform.coh.schedule.notifiers;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;

@Component
@Qualifier("BasicJsonNotificationForwarder")
public class BasicJsonNotificationForwarder implements NotificationForwarder<NotificationRequest> {

    private static final Logger log = LoggerFactory.getLogger(BasicJsonNotificationForwarder.class);

    private static final String PLACEHOLDER_HOST = "${base-urls.test-url}";

    private static final ObjectMapper mapper = new ObjectMapper();

    private static HttpHeaders URL_ENCODED_HEADER;

    static {
        URL_ENCODED_HEADER = new HttpHeaders();
        URL_ENCODED_HEADER.add("Content-Type", "application/json");
    }

    @Value("${base-urls.test-url}")
    String baseUrl;

    @Override
    public ResponseEntity sendEndpoint(SessionEventForwardingRegister register, NotificationRequest notificationRequest) throws NotificationException {

        String endpoint = register.getForwardingEndpoint();
        if (endpoint.contains(PLACEHOLDER_HOST)) {
            endpoint = endpoint.replace("${base-urls.test-url}", baseUrl);
        }

        ResponseEntity response = null;
        try {
            log.info("Sending request to " + endpoint);
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> request = new HttpEntity<>( mapper.writeValueAsString(notificationRequest), URL_ENCODED_HEADER);
            response = restTemplate.exchange(endpoint, HttpMethod.POST, request, String.class);
            log.info("Endpoint responded with " + response.getStatusCodeValue());
        } catch (JsonProcessingException e) {
            throw new NotificationException(e.getMessage());
        }  catch (HttpClientErrorException hcee) {
            throw new NotificationException("HTTP error. Endpoint responded with " + hcee.getRawStatusCode() + " and response body " + hcee.getResponseBodyAsString());
        }

        return response;
    }
}