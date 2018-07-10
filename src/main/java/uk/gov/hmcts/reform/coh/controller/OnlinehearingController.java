package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.CreateOnlinehearingResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlinehearingMapper;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlinehearingRequest;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlinehearingResponse;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.Onlinehearing;
import uk.gov.hmcts.reform.coh.domain.OnlinehearingPanelMember;
import uk.gov.hmcts.reform.coh.domain.Onlinehearingstate;
import uk.gov.hmcts.reform.coh.service.JurisdictionService;
import uk.gov.hmcts.reform.coh.service.OnlinehearingPanelMemberService;
import uk.gov.hmcts.reform.coh.service.OnlinehearingService;
import uk.gov.hmcts.reform.coh.service.OnlinehearingStateService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/continuous-online-hearings")
public class OnlinehearingController {

    /**
     * TODO - Don't hard code the starting state
     */
    private static final String STARTING_STATE = "continuous_online_hearing_started";

    @Autowired
    private OnlinehearingService onlinehearingService;

    @Autowired
    private OnlinehearingPanelMemberService onlinehearingPanelMemberService;

    @Autowired
    private OnlinehearingStateService onlinehearingStateService;

    @Autowired
    private JurisdictionService jurisdictionService;

    @ApiOperation(value = "Get Online Hearing", notes = "A GET request with a request body is used to retrieve an online hearing")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Onlinehearing.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @GetMapping(value = "{onlinehearingId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OnlinehearingResponse> retrieveOnlinehearing(@PathVariable String onlinehearingId ) {

        Onlinehearing onlinehearing = new Onlinehearing();
        onlinehearing.setOnlinehearingId(UUID.fromString(onlinehearingId));
        Optional<Onlinehearing> retrievedOnlinehearing = onlinehearingService.retrieveOnlinehearing(onlinehearing);
        if (!retrievedOnlinehearing.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        OnlinehearingResponse response = new OnlinehearingResponse();
        OnlinehearingMapper mapper = new OnlinehearingMapper(response, retrievedOnlinehearing.get());
        mapper.map();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation(value = "Create Online Hearing", notes = "A POST request is used to create an online hearing")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = CreateOnlinehearingResponse.class),
            @ApiResponse(code = 201, message = "Created", response = CreateOnlinehearingResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateOnlinehearingResponse> createOnlinehearing(@RequestBody OnlinehearingRequest body) {

        Onlinehearing onlinehearing = new Onlinehearing();

        if (StringUtils.isEmpty(body.getCaseId()) || body.getPanel() == null || body.getPanel().isEmpty()) {
            return new ResponseEntity<>( HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<Jurisdiction> jurisdiction = jurisdictionService.getJurisdictionWithName(body.getJurisdiction());
        if (!jurisdiction.isPresent()) {
            return new ResponseEntity<>( HttpStatus.UNPROCESSABLE_ENTITY);
        }

        for (OnlinehearingRequest.PanelMember member : body.getPanel()) {
            if (StringUtils.isEmpty(member.getIdentityToken()) || StringUtils.isEmpty(member.getName())) {
                return new ResponseEntity<>( HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }

        Optional<Onlinehearingstate> onlinehearingState = onlinehearingStateService.retrieveOnlinehearingStateByState(STARTING_STATE);
        if (!onlinehearingState.isPresent()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        onlinehearing.setOnlinehearingId(UUID.randomUUID());
        onlinehearing.setCaseId(body.getCaseId());
        onlinehearing.setJurisdiction(jurisdiction.get());
        onlinehearing.setStartDate(body.getStartDate());

        CreateOnlinehearingResponse response = new CreateOnlinehearingResponse();

        for (OnlinehearingRequest.PanelMember member : body.getPanel()) {
            OnlinehearingPanelMember ohpMember = new OnlinehearingPanelMember();
            ohpMember.setFullName(member.getName());
            ohpMember.setIdentityToken(member.getIdentityToken());
            ohpMember.setOnlinehearing(onlinehearing);
            onlinehearingPanelMemberService.createOnlinehearing(ohpMember);
        }

        response.setOnlinehearingId(onlinehearing.getOnlinehearingId().toString());
        onlinehearing.setOnlinehearingstate(onlinehearingState.get());
        onlinehearing.registerStateChange();
        onlinehearing = onlinehearingService.createOnlinehearing(onlinehearing);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update Online Hearing State", notes = "A POST request is used to update the state of an online hearing")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Onlinehearing.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @PatchMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Onlinehearing> updateOnlinehearingState(@RequestBody Onlinehearing body) {

        Onlinehearing onlinehearing = new Onlinehearing();

        onlinehearing.setCaseId(body.getCaseId());
        onlinehearing.setOnlinehearingstate(body.getOnlinehearingstate());

        Optional<Onlinehearing> onlinehearingOptional = onlinehearingService.retrieveOnlinehearing(onlinehearing);
        if(!onlinehearingOptional.isPresent()){
            return new ResponseEntity<Onlinehearing>(HttpStatus.BAD_REQUEST);
        }

        //onlinehearing = onlinehearingService
        //question = questionService.updateQuestion(question, body);

        return ResponseEntity.ok(onlinehearing);


    }




}
