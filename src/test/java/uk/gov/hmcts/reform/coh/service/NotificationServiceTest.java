package uk.gov.hmcts.reform.coh.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.Notification.QuestionNotification;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.repository.EventForwardingRegisterRepository;
import uk.gov.hmcts.reform.coh.repository.EventTypeRespository;

import java.util.UUID;

@RunWith(SpringRunner.class)
public class NotificationServiceTest {

    @Autowired
    private EventForwardingRegisterRepository eventForwardingRegisterRepository;

    @Autowired
    private EventTypeRespository eventTypeRespository;

    @Autowired
    private QuestionNotification questionNotification;

    @Test
    public void testSendRequestToEndPoint() {
        NotificationService notificationService = new NotificationService(eventForwardingRegisterRepository, eventTypeRespository, questionNotification);

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(UUID.fromString("1d604071-72af-4e54-94a8-d26590da97a1"));
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setJurisdictionId(1L);

        onlineHearing.setJurisdiction(jurisdiction);

        notificationService.notifyIssuedQuestionRound(onlineHearing);
    }
}
