package uk.gov.hmcts.reform.coh.task;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.events.EventTypes;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ContinuousOnlineResolutionTaskFactoryTest {

    @Autowired
    private ContinuousOnlineResolutionTaskFactory factory;

    @Test
    public void testFactoryInitialization() {
        assertNotNull(factory.getTask(EventTypes.ANSWERS_SUBMITTED.getEventType()));
        assertNotNull(factory.getTask(EventTypes.DECISION_ISSUED.getEventType()));
        assertNotNull(factory.getTask(EventTypes.QUESTION_ROUND_ISSUED.getEventType()));
    }

    @Test
    public void testInvalidKey() {
        assertNull(factory.getTask("foo"));
    }

    @Test
    public void testSetTasks() {
        ContinuousOnlineResolutionTask task = (o) -> {};
        ContinuousOnlineResolutionTaskFactory factory = new ContinuousOnlineResolutionTaskFactory();
        factory.setTasks(Arrays.asList(task));
        assertEquals(task, factory.getTask("default"));
    }
}
