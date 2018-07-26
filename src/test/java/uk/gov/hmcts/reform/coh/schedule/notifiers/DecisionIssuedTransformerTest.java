package uk.gov.hmcts.reform.coh.schedule.notifiers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.SessionEvent;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.DecisionService;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class DecisionIssuedTransformerTest {

    @Mock
    private DecisionService decisionService;

    @InjectMocks
    private DecisionIssuedTransformer transformer;

    private SessionEventType sessionEventType;

    private OnlineHearing onlineHearing;

    @Before
    public void setup() {
        transformer = new DecisionIssuedTransformer();

        sessionEventType = new SessionEventType();
        sessionEventType.setEventTypeName(EventTypes.ANSWERS_SUBMITTED.getEventType());

        onlineHearing = new OnlineHearing();
        onlineHearing.setCaseId("foo");
    }

    @Test
    public void testMapping() {
        NotificationRequest request = transformer.transform(sessionEventType, onlineHearing);
        assertEquals("foo", request.getCaseId());
    }
}
