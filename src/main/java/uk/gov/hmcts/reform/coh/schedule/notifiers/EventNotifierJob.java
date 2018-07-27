package uk.gov.hmcts.reform.coh.schedule.notifiers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private EventTransformerFactory eventTransformerFactory;

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

    private SessionEventForwardingStates successState = SessionEventForwardingStates.EVENT_FORWARDING_SUCCESS;

    @Scheduled(fixedDelayString  = "${event-scheduler.event-notifier.fixed-delay}")
    public void execute() {

        List<SessionEvent> sessionEvents = getPendingSessionEvents();
        for (SessionEvent sessionEvent : sessionEvents) {

            // Use the session event type to get the transformer that will create the notification message
            SessionEventType sessionEventType = sessionEvent.getSessionEventForwardingRegister().getSessionEventType();
            EventTransformer transformer = eventTransformerFactory.getEventTransformer(sessionEventType.getEventTypeName());
            if (transformer == null) {
                log.error(String.format("Unable to find an event transformer for %S.", sessionEventType.getEventTypeName()));
                continue;
            }

            NotificationRequest request = transformer.transform(sessionEventType, sessionEvent.getOnlineHearing());
            try {
                // Now try and send the message
                SessionEventForwardingRegister register = sessionEvent.getSessionEventForwardingRegister();
                ResponseEntity response = forwarder.sendEndpoint(register, request);

                // Probably success. Check the response code and update the statuses
                if (HttpStatus.OK.value() == response.getStatusCodeValue()) {
                    Optional<SessionEventForwardingState> success = sessionEventForwardingStateRepository.findByForwardingStateName(successState.getStateName());
                    if (success.isPresent()) {
                        sessionEvent.setSessionEventForwardingState(success.get());
                        sessionEventService.updateSessionEvent(sessionEvent);
                    }
                }
            } catch (NotificationException e) {
                log.error("Exception while trying to send a notification. Exception is " + e.getMessage());
            }
        }
    }

    private List<SessionEvent> getPendingSessionEvents() {
        Optional<SessionEventForwardingState> pending = sessionEventForwardingStateRepository.findByForwardingStateName(pendingState.getStateName());

        if (!pending.isPresent()) {
            log.error("Unable to retrieve Session Event Forwarding State: " + pendingState.getStateName());
            return Collections.emptyList();
        }

        return sessionEventService.retrieveBySessionEventForwardingState(pending.get());
    }
}