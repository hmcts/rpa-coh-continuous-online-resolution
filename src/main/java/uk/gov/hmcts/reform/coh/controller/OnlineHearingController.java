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
import uk.gov.hmcts.reform.coh.controller.utils.AuthUtils;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.controller.validators.ValidationResult;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.*;
import uk.gov.hmcts.reform.coh.states.OnlineHearingStates;

import java.time.Clock;
import java.util.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/continuous-online-hearings")
public class OnlineHearingController {

    private static final Logger log = LoggerFactory.getLogger(AnswerController.class);

    private static final String STARTING_STATE = OnlineHearingStates.STARTED.getStateName();

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private OnlineHearingStateService onlineHearingStateService;

    @Autowired
    private JurisdictionService jurisdictionService;

    @Autowired
    private SessionEventService sessionEventService;

    @Autowired
    private Clock clock;

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
            @ApiImplicitParam(name = "start_date", value = "ISO 8601 Start Date of Online Hearing", required = true)
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

        // Sonar doesn't understand that these have been tested
        if (!onlineHearingState.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Missing online hearing state configuration");
        }
        if (!jurisdiction.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Missing jurisdiction configuration");
        }

        onlineHearing.setOnlineHearingId(UUID.randomUUID());
        onlineHearing.setCaseId(body.getCaseId());
        onlineHearing.setJurisdiction(jurisdiction.get());
        onlineHearing.setStartDate(body.getStartDate());

        AuthUtils.withIdentity(onlineHearing::setOwnerReferenceId);

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

    @ApiOperation(value = "Set re-listing reason and state",
        notes = "A PUT request with a request body is used to update re-listing of online hearing.")
    @ApiResponses(value = {
        @ApiResponse(code = 202, message = "Accepted"),
        @ApiResponse(code = 401, message = "Unauthorised"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 409, message = "Conflict")
    })
    @PutMapping(value = "/{onlineHearingId}/relist", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity setRelisting(
        @PathVariable UUID onlineHearingId,
        @RequestBody @Valid RelistingRequest body
    ) {
        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearingId);
        if (!optionalOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }

        if (body.getState() == RelistingState.ISSUE_PENDING) {
            return ResponseEntity.badRequest().body("Invalid state");
        }

        OnlineHearing onlineHearing = optionalOnlineHearing.get();

        Date now = Date.from(clock.instant());

        if (onlineHearing.getRelistCreated() == null) {
            onlineHearing.setRelistCreated(now);
        }

        if (onlineHearing.getRelistState() != body.getState() || !Objects.equals(body.getReason(), onlineHearing.getRelistReason())) {
            onlineHearing.setRelistUpdated(now);
        }

        if (onlineHearing.getRelistState() == RelistingState.ISSUED
            || onlineHearing.getRelistState() == RelistingState.ISSUE_PENDING) {

            return ResponseEntity.status(HttpStatus.CONFLICT).body("Already issued");
        }

        if (body.getState() == RelistingState.ISSUED) {
            onlineHearing.setEndDate(now);
            onlineHearing.setRelistReason(body.getReason());
            onlineHearing.setRelistState(RelistingState.ISSUE_PENDING);
            sessionEventService.createSessionEvent(onlineHearing, EventTypes.ONLINE_HEARING_RELISTED.getEventType());
        } else {
            onlineHearing.setRelistReason(body.getReason());
            onlineHearing.setRelistState(body.getState());
        }
        onlineHearing.registerRelistingChange(now);
        onlineHearingService.updateOnlineHearing(onlineHearing);

        return ResponseEntity.accepted().build();
    }

    private ValidationResult validate(OnlineHearingRequest request, Optional<OnlineHearingState> onlineHearingState, Optional<Jurisdiction> jurisdiction) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        if (StringUtils.isEmpty(request.getCaseId())) {
            result.setValid(false);
            result.setReason("Case id is required");
        }

        return result;
    }
}
