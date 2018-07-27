package uk.gov.hmcts.reform.coh.schedule.notifiers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EventTransformerTest {

    @Test
    public void testEventTransformerDefaultSupports() {
        EventTransformer transformer = (s, o) -> new NotificationRequest();
        assertEquals(1, transformer.supports().size());
    }
}
