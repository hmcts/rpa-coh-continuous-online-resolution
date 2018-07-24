package uk.gov.hmcts.reform.coh.service;

import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.EventForwardingRegister;
import uk.gov.hmcts.reform.coh.repository.EventForwardingRegisterRepository;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class EventForwardingRegisterServiceTest {

    @Mock
    private EventForwardingRegisterRepository eventForwardingRegisterRepository;

    private EventForwardingRegisterService eventForwardingRegisterService;

    private EventForwardingRegister eventForwardingRegister;

    @Before
    public void setup() throws NotFoundException {
        eventForwardingRegister = new EventForwardingRegister();
        eventForwardingRegister.setEventForwardingRegisterId(1);
        eventForwardingRegisterService = new EventForwardingRegisterService(eventForwardingRegisterRepository);
    }

    @Test
    public void testCreateEventForwardingRegister() {
        when(eventForwardingRegisterRepository.save(eventForwardingRegister)).thenReturn(eventForwardingRegister);

        EventForwardingRegister newEvent = eventForwardingRegisterService.createEventForwardingRegister(eventForwardingRegister);
        assertEquals(newEvent, eventForwardingRegister);
    }

    @Test
    public void testRetrieveEventForwardingRegister() {
        when(eventForwardingRegisterRepository.findById(1)).thenReturn(Optional.of(eventForwardingRegister));
        Optional<EventForwardingRegister> newEvent = eventForwardingRegisterService.retrieveEventForwardingRegister(eventForwardingRegister);
        assertTrue(newEvent.isPresent());
    }

    @Test
    public void testRetrieveEventForwardingRegisterByIdFail() {
        when(eventForwardingRegisterRepository.findById(2)).thenReturn(Optional.empty());
        Optional<EventForwardingRegister> newEvent = eventForwardingRegisterService.retrieveEventForwardingRegister(eventForwardingRegister);
        assertFalse(newEvent.isPresent());
    }

}
