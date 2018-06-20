package uk.gov.hmcts.reform.coh.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;

import java.util.Optional;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Configuration
public class JurisdictionServiceTest {

    @Autowired
    private JurisdictionService jurisdictionService;

    @Autowired
    private JurisdictionRepository jurisdictionRepository;

    @Autowired
    private OnlineHearingRepository onlineHearingRepository;

    @Test
    public void testJurisdiction(){

        //Get online hearing object
        Optional<OnlineHearing> optOnlineHearing = onlineHearingRepository.findByExternalRef("case_id_123");
        assertNotNull(optOnlineHearing);

        jurisdictionService.issueQuestions(optOnlineHearing.get().getExternalRef());
    }

}
