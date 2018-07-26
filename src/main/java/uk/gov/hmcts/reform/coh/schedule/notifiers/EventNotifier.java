package uk.gov.hmcts.reform.coh.schedule.notifiers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.service.SessionEventService;

@Component
public class EventNotifier {

    private static final Logger log = LoggerFactory.getLogger(EventNotifier.class);

    @Autowired
    private EventTransformerManager eventTransformerManager;

    @Autowired
    private SessionEventService sessionEventService;

    @Scheduled(fixedDelayString  = "${event-scheduler.event-notifier.fixed-delay}")
    public void execute() {
    }
}