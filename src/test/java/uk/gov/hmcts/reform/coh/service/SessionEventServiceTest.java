package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingRegisterRepository;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingStateRepository;
import uk.gov.hmcts.reform.coh.repository.SessionEventRepository;
import uk.gov.hmcts.reform.coh.repository.SessionEventTypeRespository;
import uk.gov.hmcts.reform.coh.states.SessionEventForwardingStates;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class SessionEventServiceTest {

    @Mock
    private SessionEventRepository sessionEventRepository;

    @Mock
    private SessionEventTypeRespository sessionEventTypeRespository;

    @Mock
    private SessionEventForwardingStateRepository sessionEventForwardingStateRepository;

    @Mock
    private SessionEventForwardingRegisterRepository sessionEventForwardingRegisterRepository;

    @InjectMocks
    private SessionEventService sessionEventService;

    private String sessionEventTypeName = EventTypes.DECISION_ISSUED.name();

    private String forwardingStateName = SessionEventForwardingStates.EVENT_FORWARDING_PENDING.getStateName();

    private SessionEventType sessionEventType;

    private SessionEventForwardingState forwardingState;

    private SessionEventForwardingRegister sessionEventForwardingRegister;

    private SessionEvent sessionEvent;

    private OnlineHearing onlineHearing;

    private Jurisdiction jurisdiction;

    @Before
    public void setup() {

        jurisdiction = new Jurisdiction();
        jurisdiction.setJurisdictionName("foo");

        onlineHearing = new OnlineHearing();
        onlineHearing.setJurisdiction(jurisdiction);

        sessionEventType = new SessionEventType();
        sessionEventType.setEventTypeName(sessionEventTypeName);

        forwardingState = new SessionEventForwardingState();
        forwardingState.setForwardingStateName(forwardingStateName);

        sessionEventForwardingRegister = new SessionEventForwardingRegister();
        sessionEventForwardingRegister.setJurisdiction(jurisdiction);
        sessionEventForwardingRegister.setForwardingEndpoint("http://google.com");
        sessionEventForwardingRegister.setMaximumRetries(3);
        sessionEventForwardingRegister.setSessionEventType(sessionEventType);

        sessionEvent = new SessionEvent();
        sessionEvent.setOnlineHearing(onlineHearing);
        sessionEvent.setSessionEventForwardingRegister(sessionEventForwardingRegister);
        sessionEvent.setSessionEventForwardingState(forwardingState);

        given(sessionEventTypeRespository.findByEventTypeName(sessionEventTypeName)).willReturn(Optional.of(sessionEventType));
        given(sessionEventForwardingStateRepository.findByForwardingStateName(forwardingStateName)).willReturn(Optional.of(forwardingState));
        given(sessionEventForwardingRegisterRepository.findByJurisdictionAndSessionEventType(jurisdiction, sessionEventType)).willReturn(Optional.of(sessionEventForwardingRegister));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateSessionEventWithInvalidEventName() {
        given(sessionEventTypeRespository.findByEventTypeName(sessionEventTypeName)).willReturn(Optional.empty());
        sessionEventService.createSessionEvent(onlineHearing, "foo");
    }

    @Test
    public void testCreateSessionEventWithValidEventName() {
        given(sessionEventRepository.save(any(SessionEvent.class))).willReturn(sessionEvent);
        SessionEvent sessionEvent = sessionEventService.createSessionEvent(onlineHearing, sessionEventTypeName);
        assertEquals(onlineHearing, sessionEvent.getOnlineHearing());
    }

    @Test
    public void testCreateSessionEventWithSessionEventType() {
        given(sessionEventRepository.save(any(SessionEvent.class))).willReturn(sessionEvent);
        SessionEvent sessionEvent = sessionEventService.createSessionEvent(onlineHearing, sessionEventType);
        assertEquals(onlineHearing, sessionEvent.getOnlineHearing());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateSessionEventWithInvalidForwardingStateName() {
        given(sessionEventForwardingStateRepository.findByForwardingStateName(forwardingStateName)).willReturn(Optional.empty());
        sessionEventService.createSessionEvent(onlineHearing, sessionEventType);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateSessionEventWithInvalidJurisdictionAndSessionEventType() {
        given(sessionEventForwardingRegisterRepository.findByJurisdictionAndSessionEventType(onlineHearing.getJurisdiction(), sessionEventType)).willReturn(Optional.empty());
        sessionEventService.createSessionEvent(onlineHearing, sessionEventType);
    }
}
