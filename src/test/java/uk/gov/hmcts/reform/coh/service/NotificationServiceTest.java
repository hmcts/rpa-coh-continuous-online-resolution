package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.coh.Notification.Notifier;
import uk.gov.hmcts.reform.coh.controller.exceptions.NotificationException;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.repository.EventForwardingRegisterRepository;
import uk.gov.hmcts.reform.coh.repository.EventTypeRespository;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class NotificationServiceTest {

    @Mock
    private EventForwardingRegisterRepository eventForwardingRegisterRepository;

    @Mock
    private EventTypeRespository eventTypeRespository;

    @Mock
    private Notifier notifier;

    private OnlineHearing onlineHearing;
    private NotificationService notificationService;

    @Before
    public void setup() {
        EventType issuedEventType = new EventType();
        issuedEventType.setEventTypeId(1);
        issuedEventType.setEventTypeName("question_round_issued");
        given(eventTypeRespository.findByEventTypeName(anyString())).willReturn(Optional.of(issuedEventType));
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setJurisdictionId(1L);
        EventForwardingRegister eventForwardingRegister = new EventForwardingRegister(jurisdiction, issuedEventType);

        given(eventForwardingRegisterRepository.findById(any(EventForwardingRegisterId.class))).willReturn(Optional.of(eventForwardingRegister));
        given(notifier.notifyQuestionsIssued(any(EventForwardingRegister.class), any(OnlineHearing.class))).willReturn(true);
        notificationService = new NotificationService(eventForwardingRegisterRepository, eventTypeRespository, notifier);

        onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(UUID.fromString("1d604071-72af-4e54-94a8-d26590da97a1"));
        onlineHearing.setJurisdiction(jurisdiction);
    }

    @Test
    public void testSendRequestToEndPoint() {
        assertTrue(notificationService.notifyIssuedQuestionRound(onlineHearing));
    }

    @Test
    public void testNotifyMethodIsCalled() {
        notificationService.notifyIssuedQuestionRound(onlineHearing);
        verify(notifier, times(1)).notifyQuestionsIssued(any(EventForwardingRegister.class), any(OnlineHearing.class));
    }

    @Test(expected = NoSuchElementException.class)
    public void testNoSuchElementExceptionThrownIfEventRegisterNotFound() {
        given(eventForwardingRegisterRepository.findById(any(EventForwardingRegisterId.class))).willReturn(Optional.ofNullable(null));
        notificationService.notifyIssuedQuestionRound(onlineHearing);
    }

    @Test(expected = NotificationException.class)
    public void testNotificationExceptionThrownIfNotifyRequestFails() {
        given(notifier.notifyQuestionsIssued(any(EventForwardingRegister.class), any(OnlineHearing.class))).willThrow(HttpClientErrorException.class);
        notificationService.notifyIssuedQuestionRound(onlineHearing);
    }
}
