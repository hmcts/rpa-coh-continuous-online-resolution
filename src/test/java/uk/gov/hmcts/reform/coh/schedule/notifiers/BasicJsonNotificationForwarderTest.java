package uk.gov.hmcts.reform.coh.schedule.notifiers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;

import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class BasicJsonNotificationForwarderTest {

    private SessionEventForwardingRegister register;

    private BasicJsonNotificationForwarder forwarder;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ResponseEntity okResponse;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    private NotificationRequest request;

    private String google = "http://www.google.com";

    @Before
    public void setup() {
        register = new SessionEventForwardingRegister();
        register.setForwardingEndpoint(google);

        forwarder = Mockito.spy(new BasicJsonNotificationForwarder(authTokenGenerator, restTemplateBuilder));

        restTemplate = Mockito.mock(RestTemplate.class);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        mapper = Mockito.mock(ObjectMapper.class);

        okResponse = new ResponseEntity(HttpStatus.OK);

        request = new NotificationRequest();
        request.setCaseId("123");
        request.setEventType("foo");
        request.setOnlineHearingId(UUID.randomUUID());

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(okResponse);
    }

    @Test
    public void testSuccess() throws Exception {
        doReturn(restTemplate).when(forwarder).getRestTemplate();
        ResponseEntity response = forwarder.sendEndpoint(register, request);
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
    }

    @Test(expected = NotificationException.class)
    public void testExceptionThrownWhenNotHttpStatusOK() throws Exception {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenThrow(new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY));
        forwarder.sendEndpoint(register, request);
    }

    @Test
    public void testNormalEndpoint() {
        String endpoint = forwarder.refactorEndpoint(google);
        assertEquals(google, endpoint);
    }

    @Test
    public void testRefactorEndpoint() {
        doReturn("localhost").when(forwarder).getBaseUrl();
        String endpoint = forwarder.refactorEndpoint("${base-urls.test-url}/foo");
        assertEquals("localhost/foo", endpoint);
    }

    @Test
    public void testGetRestTemplate() {
        assertTrue(forwarder.getRestTemplate() instanceof RestTemplate);
    }

    @Test
    public void testCallingAuthTokenGenerator() throws Exception {
        doReturn(restTemplate).when(forwarder).getRestTemplate();

        forwarder.sendEndpoint(register, request);

        verify(authTokenGenerator, atLeastOnce()).generate();
    }

    @Test
    public void testUsingGeneratedTokenAsServiceAuthorizationHeader() throws Exception {
        doReturn(restTemplate).when(forwarder).getRestTemplate();

        String randomToken = "random token";
        doReturn(randomToken).when(authTokenGenerator).generate();

        forwarder.sendEndpoint(register, request);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), any(HttpMethod.class), captor.capture(), any(Class.class));

        assertThat(forwarder.getLastServiceAuthorization()).isEqualTo(randomToken);
    }

    @Test(expected = NotificationException.class)
    public void wrapsRestClientExceptionInNotificationException() throws Exception {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
            .thenThrow(RestClientException.class);

        forwarder.sendEndpoint(register, request);
    }
}
