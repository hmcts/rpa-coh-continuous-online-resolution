package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingPanel;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;

@RestController
@RequestMapping("/online-hearings")
public class OnlineHearingController {

    @Autowired
    OnlineHearingService onlineHearingService;

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
        for(OnlineHearingPanel panel : body.getPanel()) {
            System.out.println(panel.getName());
            System.out.println(panel.getIdentityToken());
        }
        onlineHearing.setExternalRef(body.getExternalRef());
        OnlineHearing createdOnlineHearing = onlineHearingService.createOnlineHearing(onlineHearing);

        return new ResponseEntity<>(createdOnlineHearing, HttpStatus.OK);
    }
}
