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
import uk.gov.hmcts.reform.coh.controller.onlinehearing.CreateOnlineHearingResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingRequest;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingPanelMember;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;
import uk.gov.hmcts.reform.coh.service.JurisdictionService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingPanelMemberService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/online-hearings")
public class OnlineHearingController {

    @Autowired
    OnlineHearingService onlineHearingService;

    @Autowired
    private OnlineHearingPanelMemberService onlineHearingPanelMemberService;

    @Autowired
    JurisdictionService jurisdictionService;

    @ApiOperation(value = "Get Online Hearing", notes = "A GET request with a request body is used to retrieve an online hearing")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = OnlineHearing.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @GetMapping(value = "{externalId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OnlineHearing> retrieveOnlineHearing(@PathVariable String externalId) {

        System.out.println(externalId);
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setExternalRef(externalId);
        OnlineHearing retrievedOnlineHearing = onlineHearingService.retrieveOnlineHearingByExternalRef(onlineHearing);

        return new ResponseEntity<>(retrievedOnlineHearing, HttpStatus.OK);
    }

    @ApiOperation(value = "Create Online Hearing", notes = "A POST request is used to create an online hearing")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = OnlineHearing.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OnlineHearing> createOnlineHearing(@RequestBody OnlineHearing body) {

        OnlineHearing onlineHearing = new OnlineHearing();

        Optional<Jurisdiction> jurisdiction = jurisdictionService.getJurisdictionWithName(body.getJurisdictionName());
        if(!jurisdiction.isPresent()){
            return new ResponseEntity<OnlineHearing>(HttpStatus.BAD_REQUEST);
        }
        onlineHearing.setExternalRef(body.getExternalRef());
        onlineHearing.setJurisdiction(jurisdiction.get());
        onlineHearing.setJurisdictionName(body.getJurisdictionName());

        OnlineHearing createdOnlineHearing = onlineHearingService.createOnlineHearing(onlineHearing);

        return new ResponseEntity<>(createdOnlineHearing, HttpStatus.OK);
    }

    @ApiOperation(value = "Create Online Hearing", notes = "A POST request is used to create an online hearing")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = CreateOnlineHearingResponse.class),
            @ApiResponse(code = 201, message = "Created", response = CreateOnlineHearingResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PostMapping(value = "/try", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateOnlineHearingResponse> createOnlineHearingTry(@RequestBody OnlineHearingRequest body) {

        OnlineHearing onlineHearing = new OnlineHearing();

        if (StringUtils.isEmpty(body.getCaseId())) {
            return new ResponseEntity<>( HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<Jurisdiction> jurisdiction = jurisdictionService.getJurisdictionWithName(body.getJurisdiction());
        if (!jurisdiction.isPresent()) {
            return new ResponseEntity<>( HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (body.getPanel().isEmpty()) {
            return new ResponseEntity<>( HttpStatus.UNPROCESSABLE_ENTITY);
        }

        for (OnlineHearingRequest.PanelMember member : body.getPanel()) {
            if (StringUtils.isEmpty(member.getIdentityToken()) || StringUtils.isEmpty(member.getName())) {
                return new ResponseEntity<>( HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }

        onlineHearing.setExternalRef(body.getCaseId());
        onlineHearing.setJurisdiction(jurisdiction.get());
        onlineHearing.setStartDate(body.getStartDate());


        OnlineHearing createdOnlineHearing = onlineHearingService.createOnlineHearing(onlineHearing);
        CreateOnlineHearingResponse response = new CreateOnlineHearingResponse();

        List<OnlineHearingPanelMember> panelMembers = new ArrayList<>();
        for (OnlineHearingRequest.PanelMember member : body.getPanel()) {
            OnlineHearingPanelMember ohpMember = new OnlineHearingPanelMember();
            ohpMember.setFullName(member.getName());
            ohpMember.setIdentityToken(member.getIdentityToken());
            ohpMember.setOnlineHearing(onlineHearing);
            onlineHearingPanelMemberService.createOnlineHearing(ohpMember);
        }

        response.setOnlineHearingId(createdOnlineHearing.getOnlineHearingId().toString());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
