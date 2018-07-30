package uk.gov.hmcts.reform.coh.schedule.notifiers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.events.EventTypes;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class StandardTransformerTest {

    @InjectMocks
    private StandardTransformer standardTransformer;

    private SessionEventType sessionEventType;
    private UUID uuid;
    private Date date;
    private OnlineHearing onlineHearing;

    @Before
    public void setUp() {
        sessionEventType = new SessionEventType();
        sessionEventType.setEventTypeName(EventTypes.ANSWERS_SUBMITTED.getEventType());

        uuid = UUID.randomUUID();
        date = new Date();
        onlineHearing = new OnlineHearing();
        onlineHearing.setCaseId("foo");
        onlineHearing.setOnlineHearingId(uuid);
        onlineHearing.setEndDate(date);
    }

    @Test
    public void testMapping() {
        NotificationRequest request = standardTransformer.transform(sessionEventType, onlineHearing);
        assertEquals("foo", request.getCaseId());
        assertEquals(uuid, request.getOnlineHearingId());
        assertEquals(EventTypes.ANSWERS_SUBMITTED.getEventType(), request.getEventType());
    }

    @Test
    public void testSupportsExpectedEventType() {
        List<String> supports = standardTransformer.supports();
        assertTrue(supports.contains(EventTypes.ANSWERS_SUBMITTED.getEventType()));
        assertTrue(supports.contains(EventTypes.QUESTION_DEADLINE_ELAPSED.getEventType()));
    }
}
