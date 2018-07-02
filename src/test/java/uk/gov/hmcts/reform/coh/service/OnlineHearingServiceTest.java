package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;

import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class OnlineHearingServiceTest {

    private OnlineHearingService onlineHearingService;

    @Mock
    private OnlineHearingRepository onlineHearingRepository;

    @Mock
    private OnlineHearing createdOnlineHearing;

    @Mock
    private OnlineHearingStateService onlineHearingStateService;

    private OnlineHearingState created = new OnlineHearingState("CREATED");
    private OnlineHearingState closed = new OnlineHearingState("CLOSED");


    @Before
    public void setup() {
        onlineHearingService = new OnlineHearingService(onlineHearingRepository);
        createdOnlineHearing = new OnlineHearing();
        createdOnlineHearing.setOnlineHearingId(randomUUID());
        createdOnlineHearing.setOnlineHearingState(created);
    }

    @Test
    public void testCreateOnlineHearing() {
        when(onlineHearingRepository.save(createdOnlineHearing)).thenReturn(createdOnlineHearing);
        OnlineHearing newOnlineHearing = onlineHearingService.createOnlineHearing(createdOnlineHearing);
        assertEquals(createdOnlineHearing, newOnlineHearing);
    }

    @Test
    public void testRetrieveOnlineHearing() {
        when(onlineHearingRepository.findById(any(UUID.class))).thenReturn(Optional.of(createdOnlineHearing));
        Optional<OnlineHearing> newOnlineHearing = onlineHearingService.retrieveOnlineHearing(createdOnlineHearing);
        assertTrue(newOnlineHearing.isPresent());
        assertEquals(createdOnlineHearing, newOnlineHearing.get());
    }

    @Test
    public void testRetrieveOnlineHearingByCaseId() {
        createdOnlineHearing.setCaseId("foo");
        when(onlineHearingRepository.findById(any(UUID.class))).thenReturn(Optional.of(createdOnlineHearing));
        OnlineHearing newOnlineHearing = onlineHearingService.retrieveOnlineHearingById(createdOnlineHearing.getOnlineHearingId());
        assertEquals(createdOnlineHearing, newOnlineHearing);
    }

    @Test
    public void testDeleteOnlineHearing() {
        UUID uuid = UUID.randomUUID();
        createdOnlineHearing.setOnlineHearingId(uuid);
        createdOnlineHearing.setCaseId("foo");
        doNothing().when(onlineHearingRepository).deleteById(uuid);
        onlineHearingService.deleteOnlineHearing(createdOnlineHearing);
        verify(onlineHearingRepository, times(1)).deleteById(uuid);
    }

    @Test
    public void testDeleteByCaseId() {
        String caseId = "foo";
        createdOnlineHearing.setCaseId(caseId);
        UUID onlineHearingId = createdOnlineHearing.getOnlineHearingId();
        doNothing().when(onlineHearingRepository).deleteById(onlineHearingId);
        onlineHearingService.deleteById(onlineHearingId);
        verify(onlineHearingRepository, times(1)).deleteById(onlineHearingId);
    }

    @Test
    public void testUpdateOnlineHearingState() {
        // close online hearing - can only change state from created to closed
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingState(created);

        OnlineHearing updatedOnlineHearing = new OnlineHearing();
        updatedOnlineHearing.setOnlineHearingState(closed);
        onlineHearingService.updateOnlineHearingState(onlineHearing.getOnlineHearingId(), updatedOnlineHearing);
        assertEquals(2, onlineHearing.getOnlineHearingState().getOnlineHearingStateId());
    }
}