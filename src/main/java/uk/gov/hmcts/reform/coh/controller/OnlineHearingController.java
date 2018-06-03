package uk.gov.hmcts.reform.coh.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/online-hearing")
public class OnlineHearingController {

    @RequestMapping("/retrieve")
    public String retrieve() {
        return "Greetings from Online Hearing Controller";
    }
    
}
