package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.Notification.Notifier;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.repository.EventForwardingRegisterRepository;
import uk.gov.hmcts.reform.coh.repository.EventTypeRespository;

import java.util.UUID;

@RunWith(SpringRunner.class)
public class NotificationServiceTest {

    @Mock
    private EventForwardingRegisterRepository eventForwardingRegisterRepository;

    @Mock
    private EventTypeRespository eventTypeRespository;

    @Mock
    private Notifier notifier;

    private NotificationService notificationService;

    @Before
    public void setup() {
        notificationService = new NotificationService(eventForwardingRegisterRepository, eventTypeRespository, notifier);

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(UUID.fromString("1d604071-72af-4e54-94a8-d26590da97a1"));
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setJurisdictionId(1L);

        onlineHearing.setJurisdiction(jurisdiction);
    }

    @Test
    public void testSendRequestToEndPoint() {
        notificationService.notifyIssuedQuestionRound(onlineHearing);
    }
}
