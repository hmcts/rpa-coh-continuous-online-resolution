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
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;

import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class OnlineHearingServiceTest {

    @Mock
    private OnlineHearingRepository onlineHearingRepository;

    private OnlineHearingService onlineHearingService;

    private OnlineHearing createdOnlineHearing;


    @Before
    public void setup() {
        onlineHearingService = new OnlineHearingService(onlineHearingRepository);
        createdOnlineHearing = new OnlineHearing();
        createdOnlineHearing.setOnlineHearingId(randomUUID());
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
    public void testRetrieveOnlineHearingByExternalRef() {
        createdOnlineHearing.setExternalRef("foo");
        when(onlineHearingRepository.findByExternalRef(any(String.class))).thenReturn(Optional.of(createdOnlineHearing));
        OnlineHearing newOnlineHearing = onlineHearingService.retrieveOnlineHearingByExternalRef(createdOnlineHearing);
        assertEquals(createdOnlineHearing, newOnlineHearing);
    }

    @Test
    public void testDeleteOnlineHearing() {
        UUID uuid = UUID.randomUUID();
        createdOnlineHearing.setOnlineHearingId(uuid);
        createdOnlineHearing.setExternalRef("foo");
        doNothing().when(onlineHearingRepository).deleteById(uuid);
        onlineHearingService.deleteOnlineHearing(createdOnlineHearing);
        verify(onlineHearingRepository, times(1)).deleteById(uuid);
    }

    @Test
    public void testDeleteByExternalRef() {
        String externalRef = "foo";
        createdOnlineHearing.setExternalRef(externalRef);
        doNothing().when(onlineHearingRepository).deleteByExternalRef(externalRef);
        onlineHearingService.deleteByExternalRef(externalRef);
        verify(onlineHearingRepository, times(1)).deleteByExternalRef(externalRef);
    }
}