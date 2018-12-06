package uk.gov.hmcts.reform.coh.schedule.trigger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class EventTriggerJobTest {

    @Mock
    private EventTriggerFactory eventTriggerFactory;

    @InjectMocks
    private EventTriggerJob eventTriggerJob;

    private EventTrigger reminder;
    private EventTrigger elapsed;

    @Before
    public void setUp() {
        reminder = mock(QuestionRoundReminder.class);
        Set<EventTrigger> triggers = new HashSet<>();
        triggers.add(reminder);

        elapsed = mock(QuestionRoundDeadlineElapsed.class);
        triggers.add(elapsed);
        when(eventTriggerFactory.getTriggers()).thenReturn(triggers);
    }

    @Test
    public void testExecuteIteratesThroughListOfTriggers() {
        eventTriggerJob.execute();
        verify(reminder, times(1)).execute();
        verify(elapsed, times(1)).execute();
    }
}