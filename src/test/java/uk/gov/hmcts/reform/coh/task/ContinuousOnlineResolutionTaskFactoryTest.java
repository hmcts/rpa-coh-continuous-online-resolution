package uk.gov.hmcts.reform.coh.task;


import org.junit.Before;
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

    private ContinuousOnlineResolutionTask task;
    @Before
    public void setUp() {
        factory = new ContinuousOnlineResolutionTaskFactory();
        task = (o) -> {};
        factory.setTasks(Arrays.asList(task));
    }

    @Test
    public void testGetTask() {
        assertEquals(task, factory.getTask("default"));
    }

    @Test
    public void testGetTaskNotFound() {
        assertNull(factory.getTask("foo"));
    }
}
