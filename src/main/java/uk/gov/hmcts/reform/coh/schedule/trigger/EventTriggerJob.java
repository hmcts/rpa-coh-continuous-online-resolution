package uk.gov.hmcts.reform.coh.schedule.trigger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EventTriggerJob {

    @Autowired
    private QuestionRoundDeadlineElapsed trigger;

    @Scheduled(fixedDelayString  = "${event-scheduler.event-trigger.fixed-delay}")
    public void execute() {
        trigger.execute();
    }
}
