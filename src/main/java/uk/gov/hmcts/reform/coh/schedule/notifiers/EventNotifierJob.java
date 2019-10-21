package uk.gov.hmcts.reform.coh.schedule.notifiers;

import net.javacrumbs.shedlock.core.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.appinsights.AppInsightsEventRepository;
import uk.gov.hmcts.reform.coh.appinsights.AppInsightsEvents;
import uk.gov.hmcts.reform.coh.domain.SessionEvent;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingState;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.exception.GenericException;
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

    @SchedulerLock(name = "${event-scheduler.event-notifier.lock}")
    @Scheduled(fixedDelayString  = "${event-scheduler.event-notifier.fixed-delay}")
    public void execute() {

        List<SessionEvent> sessionEvents = getPendingSessionEvents();
        log.debug("Pending session events found: {}", sessionEvents.size());
        for (SessionEvent sessionEvent : sessionEvents) {
            log.info("Processing session event: {}", sessionEvent.getEventId());

            // Use the session event type to get the transformer that will create the notification message
            SessionEventType sessionEventType = sessionEvent.getSessionEventForwardingRegister().getSessionEventType();
            EventTransformer transformer = eventTransformerFactory.getEventTransformer(sessionEventType.getEventTypeName());
            if (transformer == null) {
                log.warn("Unable to find an event transformer for {}", sessionEventType.getEventTypeName());
                continue;
            }

            log.info("Found transformer {} to handle {}.", transformer.getClass(), sessionEventType.getEventTypeName());
            NotificationRequest request = transformer.transform(sessionEventType, sessionEvent.getOnlineHearing());
            SessionEventForwardingRegister register = sessionEvent.getSessionEventForwardingRegister();
            if (register.getActive() != null && !register.getActive()) {
                log.warn("Session Event Register for jurisdiction '{}', and event type '{}' is inactive.", register.getJurisdiction().getJurisdictionName(), sessionEventType.getEventTypeName());
                continue;
            }

            try {
                // Now try and send the notification
                ResponseEntity response = forwarder.sendEndpoint(register, request);
                if (HttpStatus.OK.value() == response.getStatusCodeValue()) {
                    log.info("Register endpoint responded OK");

                    // Update the state of the session event
                    Optional<SessionEventForwardingState> success = sessionEventForwardingStateRepository.findByForwardingStateName(successState.getStateName());
                    if (success.isPresent()) {
                        log.info("Updating session event state to {}", success.get().getForwardingStateName());
                        sessionEvent.setSessionEventForwardingState(success.get());
                    }

                    // Run any tasks required after notification completes
                    ContinuousOnlineResolutionTask task = taskFactory.getTask(sessionEventType.getEventTypeName());
                    if (task != null) {
                        log.info("Found task {} to handle {}.", task.getClass(), sessionEventType.getEventTypeName());
                        task.execute(sessionEvent.getOnlineHearing());
                    }
                    sessionEventService.updateSessionEvent(sessionEvent);
                } else {
                    log.warn("Unable to send notification to endpoint: {}. Endpoint returned {}", register.getForwardingEndpoint(), response.getStatusCodeValue());
                    doFailureUpdate(register, sessionEvent);
                }
            } catch (Exception e) {
                doFailureUpdate(register, sessionEvent);
                log.error("Exception while trying to send a notification.", new GenericException(e));
            }
        }
    }

    protected void doFailureUpdate(SessionEventForwardingRegister register, SessionEvent sessionEvent) {

        sessionEvent.setRetries(sessionEvent.getRetries() + 1);
        if (sessionEvent.getRetries() >= register.getMaximumRetries()+1) {
            Optional<SessionEventForwardingState> failure = sessionEventForwardingStateRepository.findByForwardingStateName(failureState.getStateName());
            appInsightsEventRepository.trackEvent(AppInsightsEvents.COH_COR_NOTIFICATION_FAILURE.name(), createAppInsightsProperties(sessionEvent));
            if (failure.isPresent()) {
                log.info("Updating session event state to {}", failureState.getStateName());
                sessionEvent.setSessionEventForwardingState(failure.get());
            } else {
                log.warn("Unable to find session event forwarding state: {}", failureState.getStateName());
            }
        }

        sessionEventService.updateSessionEvent(sessionEvent);
    }

    private List<SessionEvent> getPendingSessionEvents() {
        Optional<SessionEventForwardingState> pending = sessionEventForwardingStateRepository.findByForwardingStateName(pendingState.getStateName());

        if (!pending.isPresent()) {
            log.warn("Unable to retrieve Session Event Forwarding State: {}", pendingState.getStateName());
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