package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.*;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.controller.validators.ValidationResult;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.*;
import uk.gov.hmcts.reform.coh.states.OnlineHearingStates;

import java.util.*;

import static uk.gov.hmcts.reform.coh.controller.utils.CommonMessages.ONLINE_HEARING_NOT_FOUND;
import static uk.gov.hmcts.reform.coh.states.OnlineHearingStates.RELISTED;

@RestController
@RequestMapping("/continuous-online-hearings")
public class OnlineHearingController {

    private static final Logger log = LoggerFactory.getLogger(AnswerController.class);

    private static final String STARTING_STATE = OnlineHearingStates.STARTED.getStateName();

    private static Set<String> permittedUpdateStates;

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private OnlineHearingStateService onlineHearingStateService;

    @Autowired
    private JurisdictionService jurisdictionService;

    @Autowired
    private SessionEventTypeService sessionEventTypeService;

    @Autowired
    private SessionEventService sessionEventService;

    @ApiOperation(value = "Get Online Hearing", notes = "A GET request with a request body is used to retrieve an online hearing")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = OnlineHearingResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @GetMapping(value = "{onlineHearingId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OnlineHearingResponse> retrieveOnlineHearing(@PathVariable String onlineHearingId) {

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(UUID.fromString(onlineHearingId));
        Optional<OnlineHearing> retrievedOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearing);
        if (!retrievedOnlineHearing.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        OnlineHearingResponse response = new OnlineHearingResponse();
        OnlineHearingMapper.map(response, retrievedOnlineHearing.get());

        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Filter for Online Hearings", notes = "A GET request with query string containing one or more instances of case_id e.g. case_id=foo&case_id=bar")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = OnlineHearingsResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @GetMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public OnlineHearingsResponse retrieveOnlineHearings(@RequestParam("case_id") List<String> caseIds,
                                                         @RequestParam("state") Optional<Set<String>> states) {

        List<OnlineHearing> onlineHearings = onlineHearingService.retrieveOnlineHearingByCaseIds(caseIds, states);

        List<OnlineHearingResponse> responses = new ArrayList<>();
        OnlineHearingsResponse onlineHearingsResponse = new OnlineHearingsResponse();
        onlineHearingsResponse.setOnlineHearingResponses(responses);
        for (OnlineHearing onlineHearing : onlineHearings) {
            OnlineHearingResponse response = new OnlineHearingResponse();
            OnlineHearingMapper.map(response, onlineHearing);
            responses.add(response);
        }

        return onlineHearingsResponse;
    }

    @ApiOperation(value = "Create Online Hearing", notes = "A POST request is used to create an online hearing")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", response = CreateOnlineHearingResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 409, message = "Duplicate case id found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "case_id", value = "The Case id", required = true),
            @ApiImplicitParam(name = "jurisdiction", value = "Accepted value is SSCS", required = true),
            @ApiImplicitParam(name = "start_date", value = "ISO 8601 Start Date of Online Hearing", required = true),
            @ApiImplicitParam(name = "panel", value = "Panel members", required = true),
            @ApiImplicitParam(name = "panel.name", value = "Name of Panel Member", required = true),
            @ApiImplicitParam(name = "panel.identity_token", value = "IDAM Token of Panel Member"),
            @ApiImplicitParam(name = "panel.role", value = "The role of the Panel Member"),
    })
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createOnlineHearing(UriComponentsBuilder uriBuilder, @RequestBody OnlineHearingRequest body) {

        if (!onlineHearingService.retrieveOnlineHearingByCaseIds(Arrays.asList(body.getCaseId())).isEmpty()) {
            return new ResponseEntity<>("Duplicate case found", HttpStatus.CONFLICT);
        }

        OnlineHearing onlineHearing = new OnlineHearing();
        Optional<OnlineHearingState> onlineHearingState = onlineHearingStateService.retrieveOnlineHearingStateByState(STARTING_STATE);
        Optional<Jurisdiction> jurisdiction = jurisdictionService.getJurisdictionWithName(body.getJurisdiction());
        ValidationResult validationResult = validate(body, onlineHearingState, jurisdiction);
        if (!validationResult.isValid()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(validationResult.getReason());
        }

        onlineHearing.setOnlineHearingId(UUID.randomUUID());
        onlineHearing.setCaseId(body.getCaseId());
        onlineHearing.setJurisdiction(jurisdiction.get());
        onlineHearing.setStartDate(body.getStartDate());

        CreateOnlineHearingResponse response = new CreateOnlineHearingResponse();

        response.setOnlineHearingId(onlineHearing.getOnlineHearingId().toString());
        onlineHearing.setOnlineHearingState(onlineHearingState.get());
        try {
            onlineHearingService.createOnlineHearing(onlineHearing);
        } catch (Exception e) {
            log.error("Could not save online hearing in database", e);
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }
        onlineHearing.registerStateChange();
        onlineHearingService.updateOnlineHearing(onlineHearing);

        UriComponents uriComponents =
                uriBuilder.path(CohUriBuilder.buildOnlineHearingGet(onlineHearing.getOnlineHearingId())).build();

        return ResponseEntity.created(uriComponents.toUri()).body(response);
    }

    @ApiOperation(value = "Update Online Hearing State", notes = "A PUT request is used to update the state of an online hearing")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 409, message = "Conflict"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PutMapping(value = "{onlineHearingId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateOnlineHearingState(@PathVariable UUID onlineHearingId, @RequestBody UpdateOnlineHearingRequest request) {

        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearingId);
        if (!optionalOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ONLINE_HEARING_NOT_FOUND);
        }

        Optional<OnlineHearingState> optionalOnlineHearingState = onlineHearingStateService.retrieveOnlineHearingStateByState(request.getState());

        if (!optionalOnlineHearingState.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Invalid state");
        }

        OnlineHearing onlineHearing = optionalOnlineHearing.get();
        if (!isPermittedUpdateState(request.getState())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Changing Online hearing state to " + request.getState() + " is not permitted");
        }
        if (!isOnlineHearingStillLive(onlineHearing)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Online hearing has already ended");
        }

        onlineHearing.setOnlineHearingState(optionalOnlineHearingState.get());
        if (RELISTED.getStateName().equals(request.getState())) {
            onlineHearing.setEndDate((new GregorianCalendar(TimeZone.getTimeZone("GMT")).getTime()));
            onlineHearing.setRelistReason(request.getReason());
            try {
                Optional<SessionEventType> sessionEventType = sessionEventTypeService.retrieveEventType(EventTypes.ONLINE_HEARING_RELISTED.getEventType());
                if (sessionEventType.isPresent()) {
                    sessionEventService.createSessionEvent(onlineHearing, sessionEventType.get());
                }
            } catch (Exception e) {
                log.debug(String.format("Unable to queue a re-list session event for '%s': %s", onlineHearing.getOnlineHearingId(), e.getMessage()));
            }
        }
        onlineHearing.registerStateChange();
        try {
            onlineHearingService.updateOnlineHearing(onlineHearing);
        } catch (Exception e) {
            log.error("Could not save online hearing in database", e);
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }

        return ResponseEntity.ok("Online hearing updated");
    }

    private synchronized boolean isPermittedUpdateState(String state) {
        if (permittedUpdateStates == null) {
            permittedUpdateStates = new HashSet<>();
            permittedUpdateStates.add(RELISTED.getStateName());
        }

        return permittedUpdateStates.contains(state);
    }

    private boolean isOnlineHearingStillLive(OnlineHearing onlineHearing) {
        return onlineHearing.getEndDate() == null;
    }

    private ValidationResult validate(OnlineHearingRequest request, Optional<OnlineHearingState> onlineHearingState, Optional<Jurisdiction> jurisdiction) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        if (StringUtils.isEmpty(request.getCaseId())) {
            result.setValid(false);
            result.setReason("Case id is required");
        } else if (!onlineHearingState.isPresent()) {
            result.setValid(false);
            result.setReason("Online hearing state is not valid");
        } else if (!jurisdiction.isPresent()) {
            result.setValid(false);
            result.setReason("Jurisdiction is not valid");
        }

        return result;
    }
}
