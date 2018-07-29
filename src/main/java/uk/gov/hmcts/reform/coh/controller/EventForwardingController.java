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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.coh.controller.events.EventRegistrationRequest;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.service.SessionEventForwardingRegisterService;
import uk.gov.hmcts.reform.coh.service.SessionEventTypeService;
import uk.gov.hmcts.reform.coh.service.JurisdictionService;

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
}
