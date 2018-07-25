package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegisterId;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingRegisterRepository;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class EventForwardingRegisterServiceTest {

    @Mock
    private SessionEventForwardingRegisterRepository sessionEventForwardingRegisterRepository;

    private EventForwardingRegisterService eventForwardingRegisterService;

    private SessionEventForwardingRegister sessionEventForwardingRegister;
    private SessionEventForwardingRegisterId sessionEventForwardingRegisterId;

    @Before
    public void setup() {
        sessionEventForwardingRegister = new SessionEventForwardingRegister();

        sessionEventForwardingRegisterId = new SessionEventForwardingRegisterId(1L, 1);
        sessionEventForwardingRegister.setEventForwardingRegisterId(sessionEventForwardingRegisterId);
        eventForwardingRegisterService = new EventForwardingRegisterService(sessionEventForwardingRegisterRepository);
    }

    @Test
    public void testCreateEventForwardingRegister() {
        when(sessionEventForwardingRegisterRepository.save(sessionEventForwardingRegister)).thenReturn(sessionEventForwardingRegister);

        SessionEventForwardingRegister newEvent = eventForwardingRegisterService.createEventForwardingRegister(sessionEventForwardingRegister);
        assertEquals(newEvent, sessionEventForwardingRegister);
    }

    @Test
    public void testRetrieveEventForwardingRegister() {
        when(sessionEventForwardingRegisterRepository.findById(sessionEventForwardingRegisterId)).thenReturn(Optional.of(sessionEventForwardingRegister));
        Optional<SessionEventForwardingRegister> newEvent = eventForwardingRegisterService.retrieveEventForwardingRegister(sessionEventForwardingRegister);
        assertTrue(newEvent.isPresent());
    }

    @Test
    public void testRetrieveEventForwardingRegisterByIdFail() {
        when(sessionEventForwardingRegisterRepository.findById(sessionEventForwardingRegisterId)).thenReturn(Optional.empty());
        Optional<SessionEventForwardingRegister> newEvent = eventForwardingRegisterService.retrieveEventForwardingRegister(sessionEventForwardingRegister);
        assertFalse(newEvent.isPresent());
    }
}
