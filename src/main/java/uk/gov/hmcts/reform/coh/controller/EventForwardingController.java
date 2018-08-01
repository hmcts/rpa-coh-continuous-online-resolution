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
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.coh.controller.events.EventRegistrationRequest;
import uk.gov.hmcts.reform.coh.controller.events.ResetSessionEventRequest;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingState;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.service.*;
import uk.gov.hmcts.reform.coh.states.SessionEventForwardingStates;

import javax.validation.Valid;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/continuous-online-hearings/events")
public class EventForwardingController {
    private static final Logger log = LoggerFactory.getLogger(EventForwardingController.class);


    private final SessionEventForwardingRegisterService sessionEventForwardingRegisterService;

    private final SessionEventTypeService sessionEventTypeService;

    private final JurisdictionService jurisdictionService;
    private final SessionEventForwardingStateService sessionEventForwardingStateService;
    private final SessionEventService sessionEventService;

    @Autowired
    public EventForwardingController(SessionEventService sessionEventService, SessionEventForwardingStateService sessionEventForwardingStateService,SessionEventForwardingRegisterService sessionEventForwardingRegisterService, SessionEventTypeService sessionEventTypeService, JurisdictionService jurisdictionService) {
        this.sessionEventForwardingRegisterService = sessionEventForwardingRegisterService;
        this.sessionEventTypeService = sessionEventTypeService;
        this.jurisdictionService = jurisdictionService;
        this.sessionEventForwardingStateService = sessionEventForwardingStateService;
        this.sessionEventService = sessionEventService;
    }

    @ApiOperation(value = "Register for event notifications", notes = "A POST request is used to register for event notifications")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 409, message = "Conflict"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity registerForEventNotifications(@Valid @RequestBody EventRegistrationRequest body) {

        Optional<SessionEventType> eventType = sessionEventTypeService.retrieveEventType(body.getEventType());
        if(!eventType.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Event type not found");
        }

        Optional<Jurisdiction> jurisdiction = jurisdictionService.getJurisdictionWithName(body.getJurisdiction());
        if(!jurisdiction.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Jurisdiction not found");
        }

        SessionEventForwardingRegister sessionEventForwardingRegister = new SessionEventForwardingRegister.Builder()
                .sessionEventType(eventType.get())
                .jurisdiction(jurisdiction.get())
                .forwardingEndpoint(body.getEndpoint())
                .maximumRetries(body.getMaxRetries())
                .withActive(true)
                .registrationDate(new Date())
                .build();

        Optional<SessionEventForwardingRegister> sessionEvent = sessionEventForwardingRegisterService.retrieveEventForwardingRegister(sessionEventForwardingRegister);

        if(sessionEvent.isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Jurisdiction already registered to event");
        } else {
            sessionEventForwardingRegisterService.createEventForwardingRegister(sessionEventForwardingRegister);
            return ResponseEntity.ok("Successfully registered for event notifications");
        }
    }

    @ApiOperation(value = "Reset session events", notes = "A PUT request is used to reset the events")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PutMapping(value = "/reset", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity resetSessionEvents(@RequestBody ResetSessionEventRequest request) {
        Optional<Jurisdiction> jurisdiction = jurisdictionService.getJurisdictionWithName(request.getJurisdiction());
        Optional<SessionEventType> sessionEventType = sessionEventTypeService.retrieveEventType(request.getEventType());

        if (!jurisdiction.isPresent() || !sessionEventType.isPresent()) {
            ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Invalid jurisdiction or event type");
        }

        SessionEventForwardingRegister eventForwardingRegister = new SessionEventForwardingRegister.Builder()
                .jurisdiction(jurisdiction.get())
                .sessionEventType(sessionEventType.get())
                .build();

        Optional<SessionEventForwardingRegister> sessionEventForwardingRegister = sessionEventForwardingRegisterService.retrieveEventForwardingRegister(eventForwardingRegister);
        Optional<SessionEventForwardingState> pendingEventForwardingState = sessionEventForwardingStateService.retrieveEventForwardingStateByName(SessionEventForwardingStates.EVENT_FORWARDING_PENDING.getStateName());

        if(!sessionEventForwardingRegister.isPresent() || !pendingEventForwardingState.isPresent()) {
            ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body("We have encounter an error. Please contact support.");
        }

        sessionEventService.retrieveAllByEventForwardingRegister(sessionEventForwardingRegister).stream()
                .filter(se -> !(se.getSessionEventForwardingState().equals(pendingEventForwardingState.get())))
                .forEach(se -> {
                            se.setSessionEventForwardingState(pendingEventForwardingState.get());
                            sessionEventService.updateSessionEvent(se);
                        });

        return ResponseEntity.status(HttpStatus.OK).body("");
    }
}
