package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.coh.controller.events.EventRegistrationRequest;
import uk.gov.hmcts.reform.coh.domain.EventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.EventType;
import uk.gov.hmcts.reform.coh.service.EventForwardingRegisterService;
import uk.gov.hmcts.reform.coh.service.EventTypeService;

import java.util.Optional;

@RestController
@RequestMapping("/continuous-online-hearings/events")
public class EventForwardingController {

    @Autowired
    private EventForwardingRegisterService eventForwardingRegisterService;

    @Autowired
    private EventTypeService eventTypeService;

    @ApiOperation(value = "Register for event notifications", notes = "A POST request is used to register for event notifications")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 409, message = "Conflict"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity registerForEventNotifications(@RequestBody EventRegistrationRequest body) {

        EventForwardingRegister eventForwardingRegister = new EventForwardingRegister();

        //Optional<EventType> eventType = eventTypeService.retrieveEventType(body)

        //eventForwardingRegisterService.createEventForwardingRegister();

        return ResponseEntity.ok("Successfully registered for event notifications");

    }


}
