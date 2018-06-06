package uk.gov.hmcts.reform.coh.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;

@RestController
@RequestMapping("/online-hearings")
public class OnlineHearingController {

    @Autowired
    OnlineHearingService onlineHearingService;

    @RequestMapping("/retrieve")
    public ResponseEntity<OnlineHearing> retrieveOnlineHearing(@RequestBody OnlineHearing body) {

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setExternalRef(body.getExternalRef());
        OnlineHearing retrievedOnlineHearing = onlineHearingService.retrieveOnlineHearingByExternalRef(onlineHearing);

        return new ResponseEntity<>(retrievedOnlineHearing, HttpStatus.OK);
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OnlineHearing> createOnlineHearing(@RequestBody OnlineHearing body) {

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setExternalRef(body.getExternalRef());
        OnlineHearing createdOnlineHearing = onlineHearingService.createOnlineHearing(onlineHearing);

        return new ResponseEntity<>(createdOnlineHearing, HttpStatus.OK);
    }
}
