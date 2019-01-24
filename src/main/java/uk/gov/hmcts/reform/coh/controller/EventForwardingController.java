package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.coh.controller.events.EventRegistrationRequest;
import uk.gov.hmcts.reform.coh.controller.events.SessionEventRequest;
import uk.gov.hmcts.reform.coh.controller.validators.ValidationResult;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.service.JurisdictionService;
import uk.gov.hmcts.reform.coh.service.SessionEventForwardingRegisterService;
import uk.gov.hmcts.reform.coh.service.SessionEventTypeService;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.Date;
import java.util.Optional;

import static uk.gov.hmcts.reform.coh.controller.utils.CommonMessages.EVENT_TYPE_NOT_FOUND;
import static uk.gov.hmcts.reform.coh.controller.utils.CommonMessages.JURISDICTION_NOT_FOUND;

@RestController
@RequestMapping("/continuous-online-hearings/events")
public class EventForwardingController {

    private static final Logger log = LoggerFactory.getLogger(EventForwardingController.class);

    private final SessionEventForwardingRegisterService sessionEventForwardingRegisterService;

    private final SessionEventTypeService sessionEventTypeService;

    private final JurisdictionService jurisdictionService;


    @Autowired
    public EventForwardingController(SessionEventForwardingRegisterService sessionEventForwardingRegisterService, SessionEventTypeService sessionEventTypeService, JurisdictionService jurisdictionService) {
        this.sessionEventForwardingRegisterService = sessionEventForwardingRegisterService;
        this.sessionEventTypeService = sessionEventTypeService;
        this.jurisdictionService = jurisdictionService;
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

        sessionEventForwardingRegisterService.saveEventForwardingRegister(eventForwardingRegister);

        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    @ApiOperation(value = "Delete event registration notification", notes = "A DELETE request is used delete to update the event registration")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 424, message = "Failed dependency")
    })
    @DeleteMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteEventNotifications(@Valid @RequestBody SessionEventRequest request) {

        ValidationResult result = validate(request);
        if (!result.isValid()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(result.getReason());
        }

        SessionEventForwardingRegister eventForwardingRegister = getSessionEventForwardingRegister(request);
        Optional<SessionEventForwardingRegister> optSessionEventForwardingRegister = sessionEventForwardingRegisterService.retrieveEventForwardingRegister(eventForwardingRegister);
        if (!optSessionEventForwardingRegister.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No register for this jurisdiction & event type");
        }
        try {
            sessionEventForwardingRegisterService.deleteEventForwardingRegister(eventForwardingRegister);
        } catch (DataIntegrityViolationException cve) {
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body("Cannot delete an event registration where events have been queue for it. Make the event registration inactive instead");
        }


        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    private ValidationResult validate(SessionEventRequest request) {

        ValidationResult result = new ValidationResult();
        result.setValid(true);
        Optional<SessionEventType> eventType = sessionEventTypeService.retrieveEventType(request.getEventType());
        if (!eventType.isPresent()) {
            result.setValid(false);
            result.setReason("Event type not found");
        }

        Optional<Jurisdiction> jurisdiction = jurisdictionService.getJurisdictionWithName(request.getJurisdiction());
        if (!jurisdiction.isPresent()) {
            result.setValid(false);
            result.setReason("Jurisdiction not found");
        }

        return result;
    }

    private SessionEventForwardingRegister getSessionEventForwardingRegister(EventRegistrationRequest request) {

        Optional<SessionEventType> eventType = sessionEventTypeService.retrieveEventType(request.getEventType());
        Optional<Jurisdiction> jurisdiction = jurisdictionService.getJurisdictionWithName(request.getJurisdiction());

        log.debug("Attempting to update event register to : {} ", request);
        return new SessionEventForwardingRegister.Builder()
                .sessionEventType(eventType.orElseThrow(() -> new EntityNotFoundException(EVENT_TYPE_NOT_FOUND)))
                .jurisdiction(jurisdiction.orElseThrow(() -> new EntityNotFoundException(JURISDICTION_NOT_FOUND)))
                .forwardingEndpoint(request.getEndpoint())
                .maximumRetries(request.getMaxRetries())
                .withActive(request.getActive() != null ? request.getActive().booleanValue() : Boolean.TRUE)
                .registrationDate(new Date())
                .build();
    }

    private SessionEventForwardingRegister getSessionEventForwardingRegister(SessionEventRequest request) {

        Optional<SessionEventType> eventType = sessionEventTypeService.retrieveEventType(request.getEventType());
        Optional<Jurisdiction> jurisdiction = jurisdictionService.getJurisdictionWithName(request.getJurisdiction());

        return new SessionEventForwardingRegister.Builder()
                .sessionEventType(eventType.orElseThrow(() -> new EntityNotFoundException(EVENT_TYPE_NOT_FOUND)))
                .jurisdiction(jurisdiction.orElseThrow(() -> new EntityNotFoundException(JURISDICTION_NOT_FOUND)))
                .build();
    }
}
