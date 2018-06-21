package uk.gov.hmcts.reform.coh.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.service.QuestionRoundService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/online-hearings/{oh_id}")
public class QuestionRoundController {

    private QuestionRoundService questionRoundService;


    @Autowired
    public QuestionRoundController(QuestionRoundService questionRoundService) {
        this.questionRoundService = questionRoundService;
    }

    @GetMapping("/questionrounds/{round_id}")
    public ResponseEntity<QuestionRound> issueQuestions(@PathVariable UUID round_id) {

        if(StringUtils.isEmpty(round_id) || round_id == null){
            return new ResponseEntity<QuestionRound>(HttpStatus.FAILED_DEPENDENCY);
        }

        Optional<QuestionRound> optQuestionRound = questionRoundService.getQuestionRound(round_id);
        if(!optQuestionRound.isPresent() || optQuestionRound == null){
            return new ResponseEntity<QuestionRound>(HttpStatus.FAILED_DEPENDENCY);
        }

        Boolean success = questionRoundService.notifyJurisdictionToIssued(optQuestionRound.get());
        if(success) {
            return ResponseEntity.ok(optQuestionRound.get());
        }else {
            return new ResponseEntity<QuestionRound>(HttpStatus.FAILED_DEPENDENCY);
        }
    }

}
