package uk.gov.hmcts.reform.coh.schedule.notifiers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.DecisionService;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class DecisionIssuedTransformerTest {

    @Mock
    private DecisionService decisionService;

    @InjectMocks
    private DecisionIssuedTransformer transformer;

    private SessionEventType sessionEventType;

    private OnlineHearing onlineHearing;

    private Decision decision;

    private UUID uuid;

    private Date date;

    @Before
    public void setup() {
        sessionEventType = new SessionEventType();
        sessionEventType.setEventTypeName(EventTypes.DECISION_ISSUED.getEventType());

        uuid = UUID.randomUUID();
        date = new Date();
        onlineHearing = new OnlineHearing();
        onlineHearing.setCaseId("foo");
        onlineHearing.setOnlineHearingId(uuid);
        onlineHearing.setEndDate(date);

        decision = new Decision();

        when(decisionService.findByOnlineHearingId(onlineHearing.getOnlineHearingId())).thenReturn(Optional.of(decision));
    }

    @Test
    public void testMapping() {
        NotificationRequest request = transformer.transform(sessionEventType, onlineHearing);
        assertEquals("foo", request.getCaseId());
        assertEquals(uuid, request.getOnlineHearingId());
        assertEquals(EventTypes.DECISION_ISSUED.getEventType(), request.getEventType());
    }

    @Test
    public void testSupports() {
        assertEquals(1, transformer.supports().size());
        assertEquals("decision_issued", transformer.supports().get(0));
    }
}
