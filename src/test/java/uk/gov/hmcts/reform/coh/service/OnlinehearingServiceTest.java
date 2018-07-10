package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.Onlinehearing;
import uk.gov.hmcts.reform.coh.repository.OnlinehearingRepository;

import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class OnlinehearingServiceTest {

    @Mock
    private OnlinehearingRepository onlinehearingRepository;

    private OnlinehearingService onlinehearingService;

    private Onlinehearing createdOnlinehearing;


    @Before
    public void setup() {
        onlinehearingService = new OnlinehearingService(onlinehearingRepository);
        createdOnlinehearing = new Onlinehearing();
        createdOnlinehearing.setOnlinehearingId(randomUUID());
    }

    @Test
    public void testCreateOnlinehearing() {
        when(onlinehearingRepository.save(createdOnlinehearing)).thenReturn(createdOnlinehearing);
        Onlinehearing newOnlinehearing = onlinehearingService.createOnlinehearing(createdOnlinehearing);
        assertEquals(createdOnlinehearing, newOnlinehearing);
    }

    @Test
    public void testRetrieveOnlinehearing() {
        when(onlinehearingRepository.findById(any(UUID.class))).thenReturn(Optional.of(createdOnlinehearing));
        Optional<Onlinehearing> newOnlinehearing = onlinehearingService.retrieveOnlinehearing(createdOnlinehearing);
        assertTrue(newOnlinehearing.isPresent());
        assertEquals(createdOnlinehearing, newOnlinehearing.get());
    }

    @Test
    public void testRetrieveOnlinehearingByCaseId() {
        createdOnlinehearing.setCaseId("foo");
        when(onlinehearingRepository.findByCaseId(any(String.class))).thenReturn(Optional.of(createdOnlinehearing));
        Onlinehearing newOnlinehearing = onlinehearingService.retrieveOnlinehearingByCaseId(createdOnlinehearing);
        assertEquals(createdOnlinehearing, newOnlinehearing);
    }

    @Test
    public void testDeleteOnlinehearing() {
        UUID uuid = UUID.randomUUID();
        createdOnlinehearing.setOnlinehearingId(uuid);
        createdOnlinehearing.setCaseId("foo");
        doNothing().when(onlinehearingRepository).deleteById(uuid);
        onlinehearingService.deleteOnlinehearing(createdOnlinehearing);
        verify(onlinehearingRepository, times(1)).deleteById(uuid);
    }

    @Test
    public void testDeleteByCaseId() {
        String caseId = "foo";
        createdOnlinehearing.setCaseId(caseId);
        doNothing().when(onlinehearingRepository).deleteByCaseId(caseId);
        onlinehearingService.deleteByCaseId(caseId);
        verify(onlinehearingRepository, times(1)).deleteByCaseId(caseId);
    }
}