package uk.gov.hmcts.reform.coh.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.gov.hmcts.reform.coh.Application;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Configuration
@ComponentScan("uk.gov.hmcts.reform.coh")
@WebAppConfiguration
public class OnlineHearingServiceTest {

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private OnlineHearingRepository onlineHearingRepository;

    @Test
    public void createOnlineHearing() {
        OnlineHearingService onlineHearingService = new OnlineHearingService(onlineHearingRepository);
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setExternalRef("TestObjectRef");
        OnlineHearing newOnlineHearing = onlineHearingService.createOnlineHearing(onlineHearing);
        assertNotNull(newOnlineHearing.getOnlineHearingId());
    }

    @Test
    public void retrieveOnlineHearing() {
    }

    @Test
    public void deleteOnlineHearing() {
    }
}