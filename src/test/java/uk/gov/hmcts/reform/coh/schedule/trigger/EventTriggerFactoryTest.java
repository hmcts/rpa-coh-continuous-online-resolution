package uk.gov.hmcts.reform.coh.schedule.trigger;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventTriggerFactoryTest {

    @Test
    public void testFactory() {
        EventTrigger et = () -> {};
        EventTriggerFactory factory = new EventTriggerFactory();
        factory.setTriggers(Arrays.asList(et));
        assertEquals(1, factory.getTriggers().size());
        assertTrue(factory.getTriggers().contains(et));
    }
}
