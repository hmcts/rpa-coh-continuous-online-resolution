package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundRequest;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundResponse;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundResponseMapper;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundsResponse;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionRoundService;
import uk.gov.hmcts.reform.coh.service.QuestionStateService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/continuous-online-hearings/{onlineHearingId}")
public class QuestionRoundController {

    @Autowired
    private QuestionRoundService questionRoundService;

    @Autowired
    private QuestionStateService questionStateService;

    @Autowired
    private OnlineHearingService onlineHearingService;

    @ApiOperation("Get all question rounds")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Question.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @GetMapping("/questionrounds")
    public ResponseEntity<QuestionRoundsResponse> getQuestionRounds(@PathVariable UUID onlineHearingId) {

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingId);
        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearing);
        if(!optionalOnlineHearing.isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        onlineHearing = optionalOnlineHearing.get();
        List<QuestionRound> questionRounds = questionRoundService.getAllQuestionRounds(onlineHearing);

        QuestionRoundsResponse questionRoundsResponse = new QuestionRoundsResponse();

        questionRoundsResponse.convertToQuestionRounds(questionRounds);
        questionRoundsResponse.setCurrentQuestionRound(questionRoundService.getCurrentQuestionRoundNumber(onlineHearing));
        questionRoundsResponse.setNextQuestionRound(questionRoundService.getNextQuestionRound(onlineHearing, questionRoundsResponse.getCurrentQuestionRound()));
        questionRoundsResponse.setMaxQuestionRound(onlineHearing.getJurisdiction().getMaxQuestionRounds());
        questionRoundsResponse.setPreviousQuestionRound(questionRoundService.getPreviousQuestionRound(questionRoundsResponse.getCurrentQuestionRound()));

        return ResponseEntity.ok(questionRoundsResponse);
    }


    @ApiOperation("Get a question round")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Question.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @GetMapping("/questionrounds/{roundId}")
    public ResponseEntity<QuestionRoundResponse> getQuestionRound(@PathVariable UUID onlineHearingId, @PathVariable int roundId) {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingId);
        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearing);
        if(!optionalOnlineHearing.isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        onlineHearing = optionalOnlineHearing.get();
        QuestionRound questionRound = questionRoundService.getQuestionRoundByRoundId(onlineHearing, roundId);

        QuestionRoundResponse questionRoundResponse = new QuestionRoundResponse();
        QuestionRoundResponseMapper.map(questionRound, questionRoundResponse);

        return ResponseEntity.ok(questionRoundResponse);
    }

    @ApiOperation("Update a question round")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Question.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PutMapping("/questionrounds/{roundId}")
    public ResponseEntity updateQuestionRound(@PathVariable UUID onlineHearingId, @PathVariable int roundId,
                                                                     @RequestBody QuestionRoundRequest body) {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingId);
        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearing);
        if(!optionalOnlineHearing.isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        int currentQuestionRoundNumber = questionRoundService.getCurrentQuestionRoundNumber(onlineHearing);
        if(roundId > currentQuestionRoundNumber) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<QuestionState> questionStateOptional = questionStateService.retrieveQuestionStateByStateName(body.getStateName());
        if(!questionStateOptional.isPresent()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!questionStateOptional.get().getState().equals("ISSUED")){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if(currentQuestionRoundNumber != roundId) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        onlineHearing = optionalOnlineHearing.get();
        questionRoundService.issueQuestionRound(onlineHearing, questionStateOptional.get(), roundId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
