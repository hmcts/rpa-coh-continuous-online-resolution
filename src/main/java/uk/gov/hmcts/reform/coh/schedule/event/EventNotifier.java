package uk.gov.hmcts.reform.coh.schedule.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EventNotifier {

    private static final Logger log = LoggerFactory.getLogger(EventNotifier.class);

    @Autowired

    @Scheduled(fixedDelayString  = "${event-scheduler.event-notifier.fixed-delay}")
    public void execute() {
        log.info("Continuous Online Hearing is running");
    }}