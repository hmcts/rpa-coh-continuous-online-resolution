package uk.gov.hmcts.reform.coh.schedule.notifiers;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class NotificationRequestTest {

    private UUID uuid = UUID.randomUUID();

    private NotificationRequest request;

    private Date date = new Date();

    @Before
    public void setup() {
        request = new NotificationRequest();

        request.setOnlineHearingId(uuid);
        request.setEventType("foo");
        request.setCaseId("case_123");
        request.setExpiryDate(date.toString());
        request.setReason("bar");
    }

    @Test
    public void testAllFields() {
        assertTrue(request.toString().contains("onlineHearingId"));
        assertTrue(request.toString().contains("eventType"));
        assertTrue(request.toString().contains("caseId"));
        assertTrue(request.toString().contains("expiryDate"));
        assertTrue(request.toString().contains("reason"));
    }

    @Test
    public void testWhenNoExpiryDate() {
        request.setExpiryDate(null);
        assertFalse(request.toString().contains("expiryDate"));
    }

    @Test
    public void testWhenNoReason() {
        request.setReason(null);
        System.out.println(request.toString());
        assertFalse(request.toString().contains("reason"));
    }
}
