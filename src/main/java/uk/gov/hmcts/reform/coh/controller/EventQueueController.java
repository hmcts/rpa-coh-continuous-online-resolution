package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.coh.controller.events.SessionEventRequest;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingState;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.exception.GenericException;
import uk.gov.hmcts.reform.coh.service.*;
import uk.gov.hmcts.reform.coh.states.SessionEventForwardingStates;
import uk.gov.hmcts.reform.logging.exception.AlertLevel;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/continuous-online-hearings/events")
public class EventQueueController {
    private static final Logger log = LoggerFactory.getLogger(EventQueueController.class);

    private final SessionEventForwardingRegisterService sessionEventForwardingRegisterService;

    private final SessionEventTypeService sessionEventTypeService;

    private final JurisdictionService jurisdictionService;

    private final SessionEventForwardingStateService sessionEventForwardingStateService;

    private final SessionEventService sessionEventService;

    @Autowired
    public EventQueueController(SessionEventService sessionEventService, SessionEventForwardingStateService sessionEventForwardingStateService, SessionEventForwardingRegisterService sessionEventForwardingRegisterService, SessionEventTypeService sessionEventTypeService, JurisdictionService jurisdictionService) {
        this.sessionEventForwardingRegisterService = sessionEventForwardingRegisterService;
        this.sessionEventTypeService = sessionEventTypeService;
        this.jurisdictionService = jurisdictionService;
        this.sessionEventForwardingStateService = sessionEventForwardingStateService;
        this.sessionEventService = sessionEventService;
    }

    @ApiOperation(value = "Reset session events", notes = "A PUT request is used to reset the events")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error"),
            @ApiResponse(code = 424, message = "Failed dependency")
    })
    @PutMapping(value = "/reset", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity resetSessionEvents(@Valid @RequestBody SessionEventRequest request) {

        Optional<SessionEventType> eventType = sessionEventTypeService.retrieveEventType(request.getEventType());
        if (!eventType.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Event type not found");
        }

        Optional<Jurisdiction> jurisdiction = jurisdictionService.getJurisdictionWithName(request.getJurisdiction());
        if (!jurisdiction.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Jurisdiction not found");
        }

        SessionEventForwardingRegister eventForwardingRegister = new SessionEventForwardingRegister.Builder()
                .jurisdiction(jurisdiction.orElseThrow(EntityNotFoundException::new))
                .sessionEventType(eventType.orElseThrow(EntityNotFoundException::new))
                .build();

        Optional<SessionEventForwardingRegister> sessionEventForwardingRegister = sessionEventForwardingRegisterService.retrieveEventForwardingRegister(eventForwardingRegister);
        if (!sessionEventForwardingRegister.isPresent()) {
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body("No registers for this jurisdiction & event type");
        }

        try {
            SessionEventForwardingState pendingEventForwardingState = getSessionEventForwardingState(SessionEventForwardingStates.EVENT_FORWARDING_PENDING);

            SessionEventForwardingState failedEventForwardingState = getSessionEventForwardingState(SessionEventForwardingStates.EVENT_FORWARDING_FAILED);

            sessionEventService.findAllBySessionEventForwardingRegisterAndSessionEventForwardingState(sessionEventForwardingRegister.get(), failedEventForwardingState).stream()                    .forEach(se -> {
                se.setSessionEventForwardingState(pendingEventForwardingState);
                se.setRetries(0);
                sessionEventService.updateSessionEvent(se);
            });
        } catch (EntityNotFoundException enfe) {
            log.error(
                "Pending event forwarding state was not found in the database.",
                new GenericException(AlertLevel.P1, enfe)
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("We have encounter an error. Please contact support.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    private SessionEventForwardingState getSessionEventForwardingState(SessionEventForwardingStates state) throws EntityNotFoundException{
        Optional<SessionEventForwardingState> eventForwardingState = sessionEventForwardingStateService.retrieveEventForwardingStateByName(state.getStateName());
        return eventForwardingState.orElseThrow(() -> new EntityNotFoundException("Unable to find SessionEventForwardingStates: " + state.getStateName()));
    }
}
