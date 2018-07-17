package uk.gov.hmcts.reform.coh.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class NotificationScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduledTask.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedDelayString  = "${event-scheduler.fixed-delay}")
    public void execute() {
        log.info("Continuous Online Hearing is running", dateFormat.format(new Date()));
    }}
