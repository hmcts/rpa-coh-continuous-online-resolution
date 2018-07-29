package uk.gov.hmcts.reform.coh.schedule.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class EventTriggerJob {

    private static final Logger log = LoggerFactory.getLogger(EventTriggerJob.class);

    @Autowired
    private QuestionRoundDeadlineElapsed trigger;

    @Autowired
    private EventTriggerFactory factory;

    @Scheduled(fixedDelayString  = "${event-scheduler.event-trigger.fixed-delay}")
    public void execute() {

        log.info("Running " + this.getClass());
        Set<EventTrigger> triggers = factory.getTriggers();
        for (EventTrigger trigger : triggers) {
            log.info(String.format("Running event trigger %s", trigger.getClass()));
            trigger.execute();
        }
    }
}
