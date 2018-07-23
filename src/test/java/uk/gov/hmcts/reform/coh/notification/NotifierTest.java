package uk.gov.hmcts.reform.coh.notification;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.Notification.NotificationRequest;
import uk.gov.hmcts.reform.coh.Notification.Notifier;
import uk.gov.hmcts.reform.coh.controller.exceptions.NotificationException;
import uk.gov.hmcts.reform.coh.domain.EventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.EventType;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;

import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class NotifierTest {

    private Notifier notifier;
    private OnlineHearing onlineHearing;
    private EventForwardingRegister eventForwardingRegister;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setup(){
        restTemplate = mock(RestTemplate.class);
        ResponseEntity responseEntity = new ResponseEntity(HttpStatus.ACCEPTED);

        given(restTemplate.postForEntity(anyString(), any(NotificationRequest.class), any())).willReturn(responseEntity);

        this.notifier = new Notifier(restTemplate);

        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setJurisdictionId(1L);
        EventType eventType = new EventType();
        eventType.setEventTypeId(1);

        eventForwardingRegister = new EventForwardingRegister(jurisdiction, eventType);
        eventForwardingRegister.setForwardingEndpoint("http://localhost:8080/someSSCSendpoint");
        onlineHearing = new OnlineHearing();
        onlineHearing.setCaseId("case_ref_123");
        onlineHearing.setOnlineHearingId(UUID.randomUUID());
    }

    @Test
    public void testNotifyQuestionRoundsSuccess() {
        assertTrue(notifier.notifyQuestionsIssued(eventForwardingRegister, onlineHearing));
    }

    @Test(expected = NotificationException.class)
    public void testPostRequestFailsAndThrowHttpException() {
        given(restTemplate.postForEntity(anyString(), any(NotificationRequest.class), any())).willReturn(new ResponseEntity<>(HttpStatus.BAD_GATEWAY));
        assertTrue(notifier.notifyQuestionsIssued(eventForwardingRegister, onlineHearing));
    }
}
