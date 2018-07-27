package uk.gov.hmcts.reform.coh.schedule.notifiers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
public class EventTransformerFactoryTest {

    private EventTransformerFactory factory;

    @Before
    public void setUp() {
        EventTransformer transformer = (s, o) -> new NotificationRequest();
        factory = new EventTransformerFactory();
        factory.setEvenTransformers(Arrays.asList(transformer));
    }

    @Test
    public void testGetTransformer() {
        EventTransformer transformer = factory.getEventTransformer("default");
        assertNotNull(transformer);
    }

    @Test
    public void testGetTransformerNotFound() {
        EventTransformer transformer = factory.getEventTransformer("foo");
        assertNull(transformer);
    }
}
