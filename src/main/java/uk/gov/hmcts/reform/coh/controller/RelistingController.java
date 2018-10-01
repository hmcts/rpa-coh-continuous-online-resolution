package uk.gov.hmcts.reform.coh.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.RelistingResponse;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.RelistingState;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(
    value = "/continuous-online-hearings/{onlineHearingId}/relist",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class RelistingController {

    @Autowired
    private OnlineHearingService onlineHearingService;

    @GetMapping
    public ResponseEntity retrieveRelisting(@PathVariable UUID onlineHearingId) {
        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearingId);
        if (!optionalOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
        OnlineHearing onlineHearing = optionalOnlineHearing.get();
        String reason = onlineHearing.getRelistReason();
        String state = onlineHearing.getRelistState().toString();
        RelistingResponse relistingResponse = new RelistingResponse(reason, state);
        return ResponseEntity.ok(relistingResponse);
    }

    @PostMapping
    public ResponseEntity createDraft(
        @PathVariable UUID onlineHearingId,
        @RequestBody RelistingResponse body
    ) {
        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearingId);
        if (!optionalOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }

        RelistingState state;
        try {
            state = RelistingState.valueOf(body.state);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid state");
        }

        OnlineHearing onlineHearing = optionalOnlineHearing.get();
        onlineHearing.setRelistReason(body.reason);
        onlineHearing.setRelistState(state);
        onlineHearingService.updateOnlineHearing(onlineHearing);

        return ResponseEntity.accepted().build();
    }
}
