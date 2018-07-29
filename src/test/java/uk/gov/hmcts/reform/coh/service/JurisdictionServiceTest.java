package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class JurisdictionServiceTest {
     private final String SSCS = "SSCS";

    @Mock
    private JurisdictionRepository jurisdictionRepository;

    private JurisdictionService jurisdictionService;

    private Jurisdiction jurisdiction;



    @Before
    public void setup() throws IOException {
        jurisdictionService = new JurisdictionService(jurisdictionRepository);
        jurisdiction = new Jurisdiction();
        jurisdiction.setJurisdictionName(SSCS);
        when(jurisdictionRepository.save(jurisdiction)).thenReturn(jurisdiction);
    }

    @Test
    public void testGetJurisdictionWithName() {

        when(jurisdictionRepository.findByJurisdictionName(SSCS)).thenReturn(Optional.ofNullable(jurisdiction));
        assertEquals(jurisdiction, jurisdictionService.getJurisdictionWithName(SSCS).get());
    }

    @Test
    public void testRetrieveQuestion() {
        when(jurisdictionRepository.findByJurisdictionName(SSCS)).thenReturn(Optional.of(jurisdiction));

        Optional<Jurisdiction> newJurisdiction = jurisdictionService.getJurisdictionWithName(SSCS);
        verify(jurisdictionRepository, times(1)).findByJurisdictionName(SSCS);
        assertEquals(jurisdiction, newJurisdiction.get());
    }

    @Test
    public void testFindByOnlineHearingIdFail() {
        String name = "JUI";
        when(jurisdictionRepository.findByJurisdictionName(name)).thenReturn(Optional.empty());
        assertFalse(jurisdictionService.getJurisdictionWithName(name).isPresent());
    }

}