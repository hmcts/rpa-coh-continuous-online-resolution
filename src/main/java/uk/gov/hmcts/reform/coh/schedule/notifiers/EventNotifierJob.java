package uk.gov.hmcts.reform.coh.schedule.notifiers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.appinsights.AppInsightsEvents;
import uk.gov.hmcts.reform.coh.appinsights.AppInsightsEventRepository;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingStateRepository;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.SessionEventService;
import uk.gov.hmcts.reform.coh.states.SessionEventForwardingStates;
import uk.gov.hmcts.reform.coh.task.ContinuousOnlineResolutionTask;
import uk.gov.hmcts.reform.coh.task.ContinuousOnlineResolutionTaskFactory;

import java.util.*;

@Component
public class EventNotifierJob {

    private static final Logger log = LoggerFactory.getLogger(EventNotifierJob.class);

    @Autowired
    private EventTransformerFactory eventTransformerFactory;

    @Autowired
    private ContinuousOnlineResolutionTaskFactory taskFactory;

    @Autowired
    private SessionEventService sessionEventService;

    @Autowired
    private SessionEventForwardingStateRepository sessionEventForwardingStateRepository;

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private AppInsightsEventRepository appInsightsEventRepository;

    @Autowired
    @Qualifier("BasicJsonNotificationForwarder")
    private  NotificationForwarder forwarder;

    private SessionEventForwardingStates pendingState = SessionEventForwardingStates.EVENT_FORWARDING_PENDING;

    private SessionEventForwardingStates successState = SessionEventForwardingStates.EVENT_FORWARDING_SUCCESS;

    private SessionEventForwardingStates failureState = SessionEventForwardingStates.EVENT_FORWARDING_FAILED;

    @Scheduled(fixedDelayString  = "${event-scheduler.event-notifier.fixed-delay}")
    public void execute() {

        List<SessionEvent> sessionEvents = getPendingSessionEvents();
        for (SessionEvent sessionEvent : sessionEvents) {
            log.info(String.format("Processing session event: %s", sessionEvent.getEventId()));

            // Use the session event type to get the transformer that will create the notification message
            SessionEventType sessionEventType = sessionEvent.getSessionEventForwardingRegister().getSessionEventType();
            EventTransformer transformer = eventTransformerFactory.getEventTransformer(sessionEventType.getEventTypeName());
            if (transformer == null) {
                log.error(String.format("Unable to find an event transformer for %s.", sessionEventType.getEventTypeName()));
                continue;
            }

            log.info(String.format("Found transformer %s to handle %s.", transformer.getClass(), sessionEventType.getEventTypeName()));
            NotificationRequest request = transformer.transform(sessionEventType, sessionEvent.getOnlineHearing());
            try {
                // Now try and send the notification
                SessionEventForwardingRegister register = sessionEvent.getSessionEventForwardingRegister();
                ResponseEntity response = forwarder.sendEndpoint(register, request);
                if (HttpStatus.OK.value() == response.getStatusCodeValue()) {
                    log.info(String.format("Register endpoint responded OK"));

                    // Update the state of the session event
                    Optional<SessionEventForwardingState> success = sessionEventForwardingStateRepository.findByForwardingStateName(successState.getStateName());
                    if (success.isPresent()) {
                        log.info(String.format("Updating session event state to %s", success.get().getForwardingStateName()));
                        sessionEvent.setSessionEventForwardingState(success.get());
                    }

                    // Run any tasks required after notification completes
                    ContinuousOnlineResolutionTask task = taskFactory.getTask(sessionEventType.getEventTypeName());
                    if (task != null) {
                        log.info(String.format("Found task %s to handle %s.", task.getClass(), sessionEventType.getEventTypeName()));
                        task.execute(sessionEvent.getOnlineHearing());
                    }
                } else {
                    log.error(String.format("Unable to send notification to endpoint: %s", register.getForwardingEndpoint()));
                    if (sessionEvent.getRetries() < register.getMaximumRetries()) {
                        sessionEvent.setRetries(sessionEvent.getRetries() + 1);
                    } else {
                        Optional<SessionEventForwardingState> failure = sessionEventForwardingStateRepository.findByForwardingStateName(failureState.getStateName());
                        appInsightsEventRepository.trackEvent(AppInsightsEvents.COH_COR_NOTIFICATION_FAILURE.name(), createAppInsightsProperties(sessionEvent));
                        if (failure.isPresent()) {
                            log.error(String.format("Unable to find session event forwarding state: %s", failureState.getStateName()));
                            sessionEvent.setSessionEventForwardingState(failure.get());
                        } else {
                            log.info(String.format("Updating session event state to %s",failureState.getStateName()));
                        }
                    }
                }

                sessionEventService.updateSessionEvent(sessionEvent);
            } catch (NotificationException e) {
                log.error(String.format("Exception while trying to send a notification. Exception is %s", e.getMessage()));
            }
        }
    }

    private List<SessionEvent> getPendingSessionEvents() {
        Optional<SessionEventForwardingState> pending = sessionEventForwardingStateRepository.findByForwardingStateName(pendingState.getStateName());

        if (!pending.isPresent()) {
            log.error(String.format("Unable to retrieve Session Event Forwarding State: %s", pendingState.getStateName()));
            return Collections.emptyList();
        }

        return sessionEventService.retrieveBySessionEventForwardingState(pending.get());
    }

    private Map<String, String> createAppInsightsProperties(SessionEvent sessionEvent) {

        Map<String, String> props = new HashMap<>();
        props.put("Session Event", sessionEvent.getSessionEventForwardingRegister().getSessionEventType().getEventTypeName());
        props.put("Jurisdiction", sessionEvent.getSessionEventForwardingRegister().getJurisdiction().getJurisdictionName());
        props.put("Endpoint", sessionEvent.getSessionEventForwardingRegister().getForwardingEndpoint());

        return props;
    }
}