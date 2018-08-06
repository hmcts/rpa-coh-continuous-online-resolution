package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingMapper;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingResponse;
import uk.gov.hmcts.reform.coh.controller.validators.Validation;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.service.JurisdictionService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingPanelMemberService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/continuous-online-hearings/{onlineHearingId}/timelines")
public class OnlineHearingTimelinesController {

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private OnlineHearingPanelMemberService onlineHearingPanelMemberService;

    @Autowired
    private OnlineHearingStateService onlineHearingStateService;

    @Autowired
    private JurisdictionService jurisdictionService;

    private Validation validation = new Validation();


    @ApiOperation(value = "Get Online Hearing Timeline", notes = "A GET request with a request body is used to retrieve an online hearing timeline")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = OnlineHearingResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @GetMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OnlineHearingResponse> retrieveOnlineHearingTimeline(@PathVariable String onlineHearingId ) {

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
}
