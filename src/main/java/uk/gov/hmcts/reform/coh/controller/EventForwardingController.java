package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.coh.controller.events.EventRegistrationRequest;
import uk.gov.hmcts.reform.coh.controller.validators.ValidationResult;
import uk.gov.hmcts.reform.coh.domain.EventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.EventType;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.service.EventForwardingRegisterService;
import uk.gov.hmcts.reform.coh.service.EventTypeService;
import uk.gov.hmcts.reform.coh.service.JurisdictionService;

import javax.validation.Validation;
import java.text.ParseException;
import java.util.Optional;

@RestController
@RequestMapping("/continuous-online-hearings/events")
public class EventForwardingController {

    @Autowired
    private EventForwardingRegisterService eventForwardingRegisterService;

    @Autowired
    private EventTypeService eventTypeService;

    @Autowired
    private JurisdictionService jurisdictionService;

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

        ValidationResult validationResult = validate(body);
        if (!validationResult.isValid()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(validationResult.getReason());
        }

        Optional<EventType> eventType = eventTypeService.retrieveEventType(body.getEventType());
        if (!eventType.isPresent()){
            return new ResponseEntity<>("Invalid event type", HttpStatus.NOT_FOUND);
        }

        Optional<Jurisdiction> jurisdiction = jurisdictionService.getJurisdictionWithName(body.getJurisdiction());
        if (!jurisdiction.isPresent()){
            return new ResponseEntity<>("No jurisdiction specified", HttpStatus.NOT_FOUND);
        }

        eventForwardingRegister.setEventType(eventType.get());
        eventForwardingRegister.setJurisdiction(jurisdiction.get());
        eventForwardingRegister.setForwardingEndpoint(body.getEndpoint());

        //if (body.getMaxRetries())
        try  {
            eventForwardingRegister.setMaximumRetries(Integer.parseInt(body.getMaxRetries()));
        } catch (Exception e) {
            return new ResponseEntity<>("Maximum retries must be an integer", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        eventForwardingRegisterService.createEventForwardingRegister(eventForwardingRegister);

        return ResponseEntity.ok("Successfully registered for event notifications");

    }

    private ValidationResult validate(EventRegistrationRequest request){
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        if (StringUtils.isEmpty(request.getEventType())) {
            result.setValid(false);
            result.setReason("Event type is required");
        } else if (StringUtils.isEmpty(request.getJurisdiction())) {
            result.setValid(false);
            result.setReason("Jurisdiction is required");
        } else if (StringUtils.isEmpty(request.getEndpoint())) {
            result.setValid(false);
            result.setReason("Forwarding endpoint is required");
        }

        return result;
    }


}
