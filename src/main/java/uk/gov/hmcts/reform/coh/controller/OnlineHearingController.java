package uk.gov.hmcts.reform.coh.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;

@RestController
@RequestMapping("/online-hearings")
public class OnlineHearingController {

    @RequestMapping("/retrieve")
    public String retrieve() {
        return "Greetings from Online Hearing Controller";
    }

    @PostMapping(value = "")
    public ResponseEntity<OnlineHearing> createOnlineHearing(@RequestBody OnlineHearing body) {
        return ResponseEntity.ok(body);
    }

}
