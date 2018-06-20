package uk.gov.hmcts.reform.coh.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.service.QuestionRoundService;

@RestController
@RequestMapping("/online-hearings/{oh_id}")
public class QuestionRoundController {
    private QuestionRoundService questionRoundService;

    @GetMapping("/questionrounds/{round_id}")
    public ResponseEntity<QuestionRound> issueQuestions(@PathVariable String oh_id, @PathVariable Integer round_id) {
        System.out.println(oh_id + ": " + round_id);

        if(StringUtils.isEmpty(oh_id) || oh_id == null){
            return (ResponseEntity<QuestionRound>) ResponseEntity.badRequest();
        }

        return ResponseEntity.ok(questionRoundService.issueQuestions(oh_id, round_id));
    }

}
