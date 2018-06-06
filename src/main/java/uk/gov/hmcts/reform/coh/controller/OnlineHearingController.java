package uk.gov.hmcts.reform.coh.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;

@RestController
@RequestMapping("/online-hearings")
public class OnlineHearingController {

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OnlineHearing> createOnlineHearing(@RequestBody OnlineHearing body) {
        return ResponseEntity.ok(body);
    }

}
