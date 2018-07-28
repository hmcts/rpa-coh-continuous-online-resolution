package uk.gov.hmcts.reform.coh.task;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
public class ContinuousOnlineResolutionTaskFactoryTest {

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
