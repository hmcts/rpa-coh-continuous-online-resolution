package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.*;
import uk.gov.hmcts.reform.coh.controller.validators.ValidationResult;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingPanelMember;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.service.JurisdictionService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingPanelMemberService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;

import javax.validation.Validation;
import java.util.*;

@RestController
@RequestMapping("/continuous-online-hearings")
public class OnlineHearingController {

    /**
     * TODO - Don't hard code the starting state
     */
    private static final String STARTING_STATE = "continuous_online_hearing_started";

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private OnlineHearingPanelMemberService onlineHearingPanelMemberService;

    @Autowired
    private OnlineHearingStateService onlineHearingStateService;

    @Autowired
    private JurisdictionService jurisdictionService;

    @ApiOperation(value = "Get Online Hearing", notes = "A GET request with a request body is used to retrieve an online hearing")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = OnlineHearing.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @GetMapping(value = "{onlineHearingId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OnlineHearingResponse> retrieveOnlineHearing(@PathVariable String onlineHearingId ) {

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(UUID.fromString(onlineHearingId));
        Optional<OnlineHearing> retrievedOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearing);
        if (!retrievedOnlineHearing.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        OnlineHearingResponse response = new OnlineHearingResponse();
        OnlineHearingMapper.map(response, retrievedOnlineHearing.get());

        return new ResponseEntity<>(response, HttpStatus.OK);
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
            @ApiImplicitParam(name = "panel.identity_token", value = "IDAM Token of Panel Member", required = true),
            @ApiImplicitParam(name = "panel.name", value = "Name of Panel Member", required = true),
    })
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createOnlineHearing(@RequestBody OnlineHearingRequest body) {

        if (!onlineHearingService.retrieveOnlineHearingByCaseIds(Arrays.asList(body.getCaseId())).isEmpty()) {
             return new ResponseEntity<>("Duplicate case found", HttpStatus.CONFLICT);
        }

        OnlineHearing onlineHearing = new OnlineHearing();
        Optional<OnlineHearingState> onlineHearingState = onlineHearingStateService.retrieveOnlineHearingStateByState(STARTING_STATE);
        Optional<Jurisdiction> jurisdiction = jurisdictionService.getJurisdictionWithName(body.getJurisdiction());
        ValidationResult validationResult = validate(body, onlineHearingState,jurisdiction );
        if (!validationResult.isValid()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(validationResult.getReason());
        }

        // Sonar doesn't understand that these have been tested
        if (!onlineHearingState.isPresent() || !jurisdiction.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Missing configuration");
        }

        Optional<OnlineHearingState> optOnlineHearingState = onlineHearingStateService.retrieveOnlineHearingStateByState(STARTING_STATE);
        if (!optOnlineHearingState.isPresent()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        onlineHearing.setOnlineHearingId(UUID.randomUUID());
        onlineHearing.setCaseId(body.getCaseId());
        onlineHearing.setJurisdiction(jurisdiction.get());
        onlineHearing.setStartDate(body.getStartDate());

        CreateOnlineHearingResponse response = new CreateOnlineHearingResponse();

        for (OnlineHearingRequest.PanelMember member : body.getPanel()) {
            OnlineHearingPanelMember ohpMember = new OnlineHearingPanelMember();
            ohpMember.setFullName(member.getName());
            ohpMember.setIdentityToken(member.getIdentityToken());
            ohpMember.setOnlineHearing(onlineHearing);
            onlineHearingPanelMemberService.createOnlineHearing(ohpMember);
        }

        response.setOnlineHearingId(onlineHearing.getOnlineHearingId().toString());
        onlineHearing.setOnlineHearingState(optOnlineHearingState.get());
        onlineHearing.registerStateChange();
        onlineHearingService.createOnlineHearing(onlineHearing);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update Online Hearing State", notes = "A PUT request is used to update the state of an online hearing")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PutMapping(value = "{onlineHearingId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateOnlineHearingState(@PathVariable UUID onlineHearingId, @RequestBody UpdateOnlineHearingRequest request) {

        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearingId);
        if (!optionalOnlineHearing.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Online hearing not found");
        }

        Optional<OnlineHearingState> optionalOnlineHearingState = onlineHearingStateService.retrieveOnlineHearingStateByState(request.getState());
        if (!optionalOnlineHearingState.isPresent()){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Invalid state");
        }

        OnlineHearing onlineHearing = optionalOnlineHearing.get();
        onlineHearing.addState(optionalOnlineHearingState.get());
        onlineHearingService.updateOnlineHearing(onlineHearing);

        return ResponseEntity.ok("Online hearing updated");
    }


    private ValidationResult validate(OnlineHearingRequest request, Optional<OnlineHearingState> onlineHearingState, Optional<Jurisdiction> jurisdiction) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        if (StringUtils.isEmpty(request.getCaseId())) {
            result.setValid(false);
            result.setReason("Case id is required");
        } else if (request.getPanel() == null || request.getPanel().isEmpty()) {
            result.setValid(false);
            result.setReason("Panel is required");
        } else if (!onlineHearingState.isPresent()) {
            result.setValid(false);
            result.setReason("Online hearing state is not valid");
        } else if (!jurisdiction.isPresent()) {
            result.setValid(false);
            result.setReason("Jurisdiction is not valid");
        } else {
            for (OnlineHearingRequest.PanelMember member : request.getPanel()) {
                if (StringUtils.isEmpty(member.getIdentityToken()) || StringUtils.isEmpty(member.getName())) {
                    result.setValid(false);
                    result.setReason("The panel member identity and name are required");
                    break;
                }
            }
        }

        return result;
    }
}
