package uk.gov.hmcts.reform.coh.schedule.notifiers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingStateRepository;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.SessionEventService;
import uk.gov.hmcts.reform.coh.states.SessionEventForwardingStates;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class EventNotifierJob {

    private static final Logger log = LoggerFactory.getLogger(EventNotifierJob.class);

    @Autowired
    private EventTransformerManager eventTransformerManager;

    @Autowired
    private SessionEventService sessionEventService;

    @Autowired
    private SessionEventForwardingStateRepository sessionEventForwardingStateRepository;

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    @Qualifier("BasicJsonNotificationForwarder")
    private  NotificationForwarder forwarder;

    private SessionEventForwardingStates pendingState = SessionEventForwardingStates.EVENT_FORWARDING_PENDING;

    @Scheduled(fixedDelayString  = "${event-scheduler.event-notifier.fixed-delay}")
    public void execute() {
        List<SessionEvent> sessionEvents = getPendingSessionEvents();

        for (SessionEvent sessionEvent : sessionEvents) {
            SessionEventType sessionEventType = sessionEvent.getSessionEventForwardingRegister().getSessionEventType();
            EventTransformer transformer = eventTransformerManager.getEventTransformer(sessionEventType.getEventTypeName());
            NotificationRequest request = transformer.transform(sessionEventType, sessionEvent.getOnlineHearing());

            SessionEventForwardingRegister register = sessionEvent.getSessionEventForwardingRegister();

            try {
                forwarder.sendEndpoint(register, request);
            } catch (NotificationException e) {
                e.printStackTrace();
            }
        }
    }

    private List<SessionEvent> getPendingSessionEvents() {
        Optional<SessionEventForwardingState> state = sessionEventForwardingStateRepository.findByForwardingStateName(pendingState.getStateName());

        if (!state.isPresent()) {
            log.error("Unable to retrieve Session Event Forwarding State: " + pendingState.getStateName());
            return Collections.emptyList();
        }

        return sessionEventService.retrieveBySessionEventForwardingState(state.get());
    }
}