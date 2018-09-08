package uk.gov.hmcts.reform.coh.schedule.notifiers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.appinsights.AppInsightsEventRepository;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingStateRepository;
import uk.gov.hmcts.reform.coh.service.SessionEventService;
import uk.gov.hmcts.reform.coh.task.ContinuousOnlineResolutionTask;
import uk.gov.hmcts.reform.coh.task.ContinuousOnlineResolutionTaskFactory;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.coh.states.SessionEventForwardingStates.*;
import static uk.gov.hmcts.reform.coh.events.EventTypes.*;

@RunWith(SpringRunner.class)
public class EventNotifierJobTest {

    @Mock
    private SessionEventService sessionEventService;

    @Mock
    private SessionEventForwardingStateRepository sessionEventForwardingStateRepository;

    @Mock
    private EventTransformerFactory transformerFactory;

    @Mock
    private ContinuousOnlineResolutionTaskFactory taskFactory;

    @Mock
    private AppInsightsEventRepository appInsightsEventRepository;

    @Mock
    @Qualifier("BasicJsonNotificationForwarder")
    private NotificationForwarder forwarder;

    @InjectMocks
    private EventNotifierJob job;

    private SessionEventForwardingState pendingState;

    private SessionEventForwardingState successState;

    private SessionEventForwardingRegister register;

    private SessionEvent sessionEvent;

    private NotificationRequest request;

    @Before
    public void setUp() throws NotificationException {

        pendingState = new SessionEventForwardingState();
        pendingState.setForwardingStateName(EVENT_FORWARDING_PENDING.getStateName());

        successState = new SessionEventForwardingState();
        successState.setForwardingStateName(EVENT_FORWARDING_SUCCESS.getStateName());

        SessionEventForwardingState failedState = new SessionEventForwardingState();
        failedState.setForwardingStateName(EVENT_FORWARDING_FAILED.getStateName());

        SessionEventType sessionEventType = new SessionEventType();
        sessionEventType.setEventTypeName(DECISION_ISSUED.getEventType());

        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setJurisdictionName("foo");

        register = new SessionEventForwardingRegister();
        register.setForwardingEndpoint("http://www.foo.com");
        register.setSessionEventType(sessionEventType);
        register.setJurisdiction(jurisdiction);
        register.setMaximumRetries(1);
        register.setActive(true);

        sessionEvent = new SessionEvent();
        sessionEvent.setSessionEventForwardingState(pendingState);
        sessionEvent.setEventId(UUID.randomUUID());
        sessionEvent.setSessionEventForwardingRegister(register);

        EventTransformer transformer = (s, o) -> request;

        ContinuousOnlineResolutionTask task = (o) -> {};

        ResponseEntity okResponse = new ResponseEntity(HttpStatus.OK);

        given(sessionEventForwardingStateRepository.findByForwardingStateName(EVENT_FORWARDING_PENDING.getStateName())).willReturn(Optional.of(pendingState));
        given(sessionEventForwardingStateRepository.findByForwardingStateName(EVENT_FORWARDING_FAILED.getStateName())).willReturn(Optional.of(failedState));
        given(sessionEventService.retrieveBySessionEventForwardingState(pendingState)).willReturn(Arrays.asList(sessionEvent));
        given(transformerFactory.getEventTransformer(DECISION_ISSUED.getEventType())).willReturn(transformer);
        given(taskFactory.getTask(DECISION_ISSUED.getEventType())).willReturn(task);
        given(forwarder.sendEndpoint(register, request)).willReturn(okResponse);
    }

    @Test
    public void testReadScheduleConfig() {
        given(sessionEventForwardingStateRepository.findByForwardingStateName(EVENT_FORWARDING_PENDING.getStateName())).willReturn(Optional.empty());
        job.execute();
        assertEquals(EVENT_FORWARDING_PENDING.getStateName(), sessionEvent.getSessionEventForwardingState().getForwardingStateName());
    }

    @Test
    public void testNoPendingState() {
        given(sessionEventForwardingStateRepository.findByForwardingStateName(EVENT_FORWARDING_PENDING.getStateName())).willReturn(Optional.empty());
        job.execute();
        assertEquals(EVENT_FORWARDING_PENDING.getStateName(), sessionEvent.getSessionEventForwardingState().getForwardingStateName());
    }

    @Test
    public void testNoSessionEventsInPendingState() {
        given(sessionEventService.retrieveBySessionEventForwardingState(pendingState)).willReturn(Arrays.asList());
        job.execute();
        assertEquals(EVENT_FORWARDING_PENDING.getStateName(), sessionEvent.getSessionEventForwardingState().getForwardingStateName());
    }

    @Test
    public void testNoEventTransformerForSessionEvents() {
        given(transformerFactory.getEventTransformer(DECISION_ISSUED.getEventType())).willReturn(null);
        job.execute();
        assertEquals(EVENT_FORWARDING_PENDING.getStateName(), sessionEvent.getSessionEventForwardingState().getForwardingStateName());
    }

    @Test
    public void testHttpFailure() throws NotificationException {
        ResponseEntity failureResponse = new ResponseEntity(HttpStatus.NOT_FOUND);
        given(forwarder.sendEndpoint(register, request)).willReturn(failureResponse);
        job.execute();
        assertEquals(EVENT_FORWARDING_PENDING.getStateName(), sessionEvent.getSessionEventForwardingState().getForwardingStateName());
    }

    @Test
    public void testHttpFailureExceedRetries() throws NotificationException {
        register.setMaximumRetries(0);
        ResponseEntity failureResponse = new ResponseEntity(HttpStatus.NOT_FOUND);
        doNothing().when(appInsightsEventRepository).trackEvent(anyString(), anyMap());
        given(forwarder.sendEndpoint(register, request)).willReturn(failureResponse);
        job.execute();
        assertEquals(EVENT_FORWARDING_FAILED.getStateName(), sessionEvent.getSessionEventForwardingState().getForwardingStateName());
        verify(appInsightsEventRepository, times(1)).trackEvent(anyString(), anyMap());
    }

    @Test
    public void testNoForwardingFailureState() throws NotificationException {
        register.setMaximumRetries(0);
        ResponseEntity failureResponse = new ResponseEntity(HttpStatus.NOT_FOUND);
        doNothing().when(appInsightsEventRepository).trackEvent(anyString(), anyMap());
        given(forwarder.sendEndpoint(register, request)).willReturn(failureResponse);
        given(sessionEventForwardingStateRepository.findByForwardingStateName(EVENT_FORWARDING_FAILED.getStateName())).willReturn(Optional.empty());
        job.execute();
        assertEquals(EVENT_FORWARDING_PENDING.getStateName(), sessionEvent.getSessionEventForwardingState().getForwardingStateName());
        verify(appInsightsEventRepository, times(1)).trackEvent(anyString(), anyMap());
    }

    @Test
    public void testNoSessionEventsInSuccessState() {
        given(sessionEventForwardingStateRepository.findByForwardingStateName(EVENT_FORWARDING_SUCCESS.getStateName())).willReturn(Optional.empty());
        job.execute();
        assertEquals(EVENT_FORWARDING_PENDING.getStateName(), sessionEvent.getSessionEventForwardingState().getForwardingStateName());
    }

    @Test
    public void testSuccess() {
        given(sessionEventForwardingStateRepository.findByForwardingStateName(EVENT_FORWARDING_SUCCESS.getStateName())).willReturn(Optional.of(successState));
        job.execute();
        assertEquals(EVENT_FORWARDING_SUCCESS.getStateName(), sessionEvent.getSessionEventForwardingState().getForwardingStateName());
    }

    @Test
    public void testRegisterNotActive() throws Exception {
        register.setActive(false);
        job.execute();
        verify(forwarder, times(0)).sendEndpoint(any(SessionEventForwardingRegister.class), any());
    }
}
