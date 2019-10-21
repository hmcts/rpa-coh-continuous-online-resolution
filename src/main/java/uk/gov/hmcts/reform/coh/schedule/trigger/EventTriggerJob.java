package uk.gov.hmcts.reform.coh.schedule.trigger;

import net.javacrumbs.shedlock.core.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventTriggerJob {

    private static final Logger log = LoggerFactory.getLogger(EventTriggerJob.class);

    @Autowired
    private EventTriggerFactory factory;

    @SchedulerLock(name = "${event-scheduler.event-trigger.lock}")
    @Scheduled(cron  = "${event-scheduler.event-trigger.cron}")
    public void execute() {

        log.info("Running " + this.getClass());
        List<EventTrigger> triggers = factory.getTriggers().stream().sorted(Comparator.comparing(EventTrigger::order)).collect(Collectors.toList());
        for (EventTrigger trigger : triggers) {
            log.info("Running event trigger {}", trigger.getClass());
            trigger.execute();
        }
    }
}
