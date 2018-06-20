package uk.gov.hmcts.reform.coh.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Configuration
public class OnlineHearingServiceTest {

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private OnlineHearingRepository onlineHearingRepository;

    private OnlineHearing createdOnlineHearing;
    private OnlineHearing retrievedOnlineHearing;


    // Will be replaced by cucumber tests!!
    @Test
    public void createOnlineHearingAndDeleteOnlineHearingAndReadOnlineHearing() {
//        OnlineHearing onlineHearing = new OnlineHearing();
//        onlineHearing.setExternalRef("TestObjectRef");
//
//        //Create
//        createdOnlineHearing = onlineHearingService.createOnlineHearing(onlineHearing);
//        assertNotNull(createdOnlineHearing.getOnlineHearingId());
//
//        //Read
//        retrievedOnlineHearing = onlineHearingService.retrieveOnlineHearingByExternalRef(onlineHearing);
//        assertNotNull(retrievedOnlineHearing);
//
//        //Delete
//        System.out.println(onlineHearing.toString());
//        onlineHearingService.deleteOnlineHearingByExternalRef(onlineHearing);
//
//        //Check deleted
//        retrievedOnlineHearing = onlineHearingService.retrieveOnlineHearingByExternalRef(onlineHearing);
//        assertNull(retrievedOnlineHearing);
    }
}