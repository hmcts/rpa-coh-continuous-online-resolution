package uk.gov.hmcts.reform.coh.schedule.notifiers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.events.EventTypes;

import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
public class EventTransformerFactoryTest {

    private EventTransformerFactory factory;

    @Before
    public void setUp() {
        factory = new EventTransformerFactory();
        factory.setEvenTransformers(Arrays.asList(new DecisionIssuedTransformer()));
    }

    @Test
    public void testGetTransformer() {
        EventTransformer transformer = factory.getEventTransformer(EventTypes.DECISION_ISSUED.getEventType());
        assertNotNull(transformer);
    }

    @Test
    public void testGetTransformerNotFound() {
        EventTransformer transformer = factory.getEventTransformer("foo");
        assertNull(transformer);
    }
}
