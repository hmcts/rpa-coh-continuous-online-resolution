package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.service.JurisdictionService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;

import java.util.Optional;

@RestController
@RequestMapping("/online-hearings")
public class OnlineHearingController {

    @Autowired
    OnlineHearingService onlineHearingService;

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
        onlineHearing.setCaseId(externalId);
        OnlineHearing retrievedOnlineHearing = onlineHearingService.retrieveOnlineHearingByCaseId(onlineHearing);

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
}
