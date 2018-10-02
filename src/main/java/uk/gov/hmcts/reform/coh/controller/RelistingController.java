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
import uk.gov.hmcts.reform.coh.controller.onlinehearing.Relisting;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.domain.RelistingState;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;
import uk.gov.hmcts.reform.coh.service.SessionEventService;
import uk.gov.hmcts.reform.coh.states.OnlineHearingStates;

import java.util.Date;
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
    private OnlineHearingStateService onlineHearingStateService;

    @Autowired
    private SessionEventService sessionEventService;

    @GetMapping
    public ResponseEntity retrieveRelisting(@PathVariable UUID onlineHearingId) {
        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearingId);
        if (!optionalOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
        OnlineHearing onlineHearing = optionalOnlineHearing.get();
        String reason = onlineHearing.getRelistReason();
        RelistingState state = onlineHearing.getRelistState();
        Relisting relisting = new Relisting(reason, state);
        return ResponseEntity.ok(relisting);
    }

    @PostMapping
    public ResponseEntity createDraft(
        @PathVariable UUID onlineHearingId,
        @RequestBody @Valid Relisting body
    ) {
        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearingId);
        if (!optionalOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }

        OnlineHearing onlineHearing = optionalOnlineHearing.get();

        if (onlineHearing.getRelistState() == RelistingState.ISSUED) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Already issued");
        }

        if (body.state == RelistingState.ISSUED) {
            onlineHearing.setEndDate(new Date());

            Optional<OnlineHearingState> optionalOnlineHearingState = onlineHearingStateService
                .retrieveOnlineHearingStateByState(OnlineHearingStates.RELISTED.getStateName());

            optionalOnlineHearingState.ifPresent(onlineHearing::setOnlineHearingState);

            sessionEventService.createSessionEvent(onlineHearing, EventTypes.ONLINE_HEARING_RELISTED.getEventType());
        }
        onlineHearing.setRelistReason(body.reason);
        onlineHearing.setRelistState(body.state);
        onlineHearingService.updateOnlineHearing(onlineHearing);

        return ResponseEntity.accepted().build();
    }
}
