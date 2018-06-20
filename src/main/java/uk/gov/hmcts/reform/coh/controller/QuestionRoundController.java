package uk.gov.hmcts.reform.coh.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.service.JurisdictionService;

import java.util.UUID;

@RestController
@RequestMapping("/online-hearings/{oh_id}")
public class QuestionRoundController {
    private JurisdictionService jurisdictionService;

    @GetMapping("/questionrounds/{round_id}")
    public ResponseEntity<QuestionRound> issueQuestions(@PathVariable String oh_id, @PathVariable UUID round_id) {
        return jurisdictionService.issueQuestions(oh_id);
    }

}
