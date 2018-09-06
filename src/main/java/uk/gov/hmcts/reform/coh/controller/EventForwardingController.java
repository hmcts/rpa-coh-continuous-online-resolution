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
import uk.gov.hmcts.reform.coh.controller.validators.ValidationResult;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingState;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.service.*;
import uk.gov.hmcts.reform.coh.states.SessionEventForwardingStates;

import javax.persistence.EntityNotFoundException;
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

        ValidationResult result = validate(body);
        if (!result.isValid()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(result.getReason());
        }

        SessionEventForwardingRegister sessionEventForwardingRegister = getSessionEventForwardingRegister(body);
        Optional<SessionEventForwardingRegister> sessionEvent = sessionEventForwardingRegisterService.retrieveEventForwardingRegister(getSessionEventForwardingRegister(body));

        if (sessionEvent.isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Jurisdiction already registered to event");
        } else {
            sessionEventForwardingRegisterService.saveEventForwardingRegister(sessionEventForwardingRegister);
            return ResponseEntity.ok("Successfully registered for event notifications");
        }
    }

    @ApiOperation(value = "Update event registration notification", notes = "A PUT request is used to update the event registration")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 424, message = "Failed dependency")
    })
    @PutMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateEventNotifications(@Valid @RequestBody EventRegistrationRequest request) {

        ValidationResult result = validate(request);
        if (!result.isValid()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(result.getReason());
        }

        SessionEventForwardingRegister eventForwardingRegister = getSessionEventForwardingRegister(request);
        Optional<SessionEventForwardingRegister> optSessionEventForwardingRegister = sessionEventForwardingRegisterService.retrieveEventForwardingRegister(eventForwardingRegister);
        if (!optSessionEventForwardingRegister.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No register for this jurisdiction & event type");
        }

        SessionEventForwardingRegister register = optSessionEventForwardingRegister.get();
        register.setForwardingEndpoint(request.getEndpoint());
        sessionEventForwardingRegisterService.saveEventForwardingRegister(register);

        return ResponseEntity.status(HttpStatus.OK).body("");
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
    public ResponseEntity resetSessionEvents(@Valid @RequestBody ResetSessionEventRequest request) {

        Optional<SessionEventType> eventType = sessionEventTypeService.retrieveEventType(request.getEventType());
        if (!eventType.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Event type not found");
        }

        Optional<Jurisdiction> jurisdiction = jurisdictionService.getJurisdictionWithName(request.getJurisdiction());
        if (!jurisdiction.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Jurisdiction not found");
        }

        SessionEventForwardingRegister eventForwardingRegister = new SessionEventForwardingRegister.Builder()
                .jurisdiction(jurisdiction.get())
                .sessionEventType(eventType.get())
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
            log.error("Pending event forwarding state was not found in the database. Exception is " + enfe);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("We have encounter an error. Please contact support.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    private ValidationResult validate(EventRegistrationRequest request) {

        ValidationResult result = new ValidationResult();
        result.setValid(false);
        Optional<SessionEventType> eventType = sessionEventTypeService.retrieveEventType(request.getEventType());
        if (!eventType.isPresent()) {
            result.setReason("Event type not found");
        }

        Optional<Jurisdiction> jurisdiction = jurisdictionService.getJurisdictionWithName(request.getJurisdiction());
        if (!jurisdiction.isPresent()) {
            result.setReason("Jurisdiction not found");
        }

        result.setValid(true);
        return result;
    }

    private SessionEventForwardingRegister getSessionEventForwardingRegister(EventRegistrationRequest request) {

        Optional<SessionEventType> eventType = sessionEventTypeService.retrieveEventType(request.getEventType());
        Optional<Jurisdiction> jurisdiction = jurisdictionService.getJurisdictionWithName(request.getJurisdiction());

        SessionEventForwardingRegister sessionEventForwardingRegister = new SessionEventForwardingRegister.Builder()
                .sessionEventType(eventType.get())
                .jurisdiction(jurisdiction.get())
                .forwardingEndpoint(request.getEndpoint())
                .maximumRetries(request.getMaxRetries())
                .withActive(true)
                .registrationDate(new Date())
                .build();

        return sessionEventForwardingRegister;
    }

    private SessionEventForwardingState getSessionEventForwardingState(SessionEventForwardingStates state) throws EntityNotFoundException{
        Optional<SessionEventForwardingState> eventForwardingState = sessionEventForwardingStateService.retrieveEventForwardingStateByName(state.getStateName());
        return eventForwardingState.orElseThrow(() -> new EntityNotFoundException("Unable to find SessionEventForwardingStates: " + state.getStateName()));
    }
}
