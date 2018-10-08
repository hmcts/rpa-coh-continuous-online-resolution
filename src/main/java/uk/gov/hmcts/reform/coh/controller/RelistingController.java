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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.RelistingRequest;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.RelistingResponse;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.RelistingState;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.SessionEventService;

import java.time.Clock;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.validation.Valid;

@RestController
@RequestMapping(
    value = "/continuous-online-hearings/{onlineHearingId}/relist",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class RelistingController {

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private SessionEventService sessionEventService;

    @Autowired
    private Clock clock;

    @ApiOperation(value = "Get re-listing", notes = "A GET request to retrieve a re-listing.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = RelistingResponse.class),
        @ApiResponse(code = 401, message = "Unauthorised"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Not Found")
    })
    @GetMapping
    public ResponseEntity retrieveRelisting(@PathVariable UUID onlineHearingId) {
        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearingId);
        if (!optionalOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
        OnlineHearing onlineHearing = optionalOnlineHearing.get();
        RelistingResponse response = new RelistingResponse(
            onlineHearing.getRelistReason(),
            onlineHearing.getRelistState(),
            onlineHearing.getRelistCreated(),
            onlineHearing.getRelistUpdated()
        );
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Set re-listing reason and state",
        notes = "A POST request with a request body is used to update re-listing of online hearing.")
    @ApiResponses(value = {
        @ApiResponse(code = 202, message = "Accepted"),
        @ApiResponse(code = 401, message = "Unauthorised"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 409, message = "Conflict")
    })
    @PutMapping
    public ResponseEntity setRelisting(
        @PathVariable UUID onlineHearingId,
        @RequestBody @Valid RelistingRequest body
    ) {
        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearingId);
        if (!optionalOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }

        if (body.state == RelistingState.ISSUE_PENDING) {
            return ResponseEntity.badRequest().body("Invalid state");
        }

        OnlineHearing onlineHearing = optionalOnlineHearing.get();

        Date now = Date.from(clock.instant());

        if (onlineHearing.getRelistCreated() == null) {
            onlineHearing.setRelistCreated(now);
        }

        if (onlineHearing.getRelistState() != body.state || !Objects.equals(body.reason, onlineHearing.getRelistReason())) {
            onlineHearing.setRelistUpdated(now);
        }

        if (onlineHearing.getRelistState() == RelistingState.ISSUED
            || onlineHearing.getRelistState() == RelistingState.ISSUE_PENDING) {

            return ResponseEntity.status(HttpStatus.CONFLICT).body("Already issued");
        }

        if (body.state == RelistingState.ISSUED) {
            onlineHearing.setEndDate(now);
            onlineHearing.setRelistReason(body.reason);
            onlineHearing.setRelistState(RelistingState.ISSUE_PENDING);
            sessionEventService.createSessionEvent(onlineHearing, EventTypes.ONLINE_HEARING_RELISTED.getEventType());
        } else {
            onlineHearing.setRelistReason(body.reason);
            onlineHearing.setRelistState(body.state);
        }
        onlineHearingService.updateOnlineHearing(onlineHearing);

        return ResponseEntity.accepted().build();
    }
}
