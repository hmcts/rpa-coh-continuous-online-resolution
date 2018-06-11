package uk.gov.hmcts.reform.coh.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;

@RestController
@RequestMapping("/online-hearings")
public class QuestionRoundController {

    @PostMapping(value = "{online_hearing_id}/question-rounds", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuestionRound> createQuestionRound(@RequestBody QuestionRound questionRound) {

        return ResponseEntity.ok(questionRound);
    }

    @GetMapping(value = "{online_hearing_id}/question-rounds", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuestionRound> retrieveQuestionRound(@RequestBody QuestionRound questionRound) {

        return ResponseEntity.ok(questionRound);
    }

}
