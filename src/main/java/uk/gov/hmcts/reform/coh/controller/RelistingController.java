package uk.gov.hmcts.reform.coh.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.RelistingResponse;

import java.util.UUID;

@RestController
@RequestMapping(
    value = "/continuous-online-hearings/{onlineHearingId}/relist",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class RelistingController {

    @GetMapping
    public ResponseEntity retrieveRelisting(@PathVariable UUID onlineHearingId) {
        String reason = "";
        String state = "DRAFTED";
        RelistingResponse relistingResponse = new RelistingResponse(reason, state);
        return ResponseEntity.ok(relistingResponse);
    }

    @PostMapping
    public ResponseEntity createDraft(
        @PathVariable UUID onlineHearingId,
        @RequestBody RelistingResponse body
    ) {
        return ResponseEntity.accepted().build();
    }
}
