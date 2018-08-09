package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.coh.controller.exceptions.NotAValidUpdateException;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundRequest;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundResponse;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundResponseMapper;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundsResponse;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.*;

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

    @Autowired
    private QuestionService questionService;

    @Autowired
    private SessionEventService sessionEventService;

    @ApiOperation("Get all question rounds")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = QuestionRoundsResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @GetMapping("/questionrounds")
    public ResponseEntity getQuestionRounds(@PathVariable UUID onlineHearingId) {

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingId);
        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearing);
        if(!optionalOnlineHearing.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Online hearing not found");
        }

        onlineHearing = optionalOnlineHearing.get();
        List<QuestionRound> questionRounds = questionRoundService.getAllQuestionRounds(onlineHearing);

        for (QuestionRound round : questionRounds) {
            if (questionRoundService.hasAllQuestionsAnswered(round)) {
                round.setQuestionRoundState("questions_answered");
            }
        }

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
    public ResponseEntity getQuestionRound(@PathVariable UUID onlineHearingId, @PathVariable int roundId) {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingId);
        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearing);
        if(!optionalOnlineHearing.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Online hearing not found");
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Online hearing not found");
        }
        onlineHearing = optionalOnlineHearing.get();

        int currentQuestionRoundNumber = questionRoundService.getCurrentQuestionRoundNumber(onlineHearing);
        if(roundId > currentQuestionRoundNumber) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Question round not found");
        }

        Optional<QuestionState> questionStateOptional = questionStateService.retrieveQuestionStateByStateName(body.getStateName());
        if(!questionStateOptional.isPresent() || (!questionStateOptional.get().getState().equals(QuestionRoundService.ISSUE_PENDING))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid question round state");
        }

        if(currentQuestionRoundNumber != roundId) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Previous question rounds cannot be issued");
        }

        QuestionRoundState qrState = questionRoundService.retrieveQuestionRoundState(questionRoundService.getQuestionRoundByRoundId(onlineHearing, currentQuestionRoundNumber));
        if(questionRoundService.alreadyIssued(qrState)){
            throw new NotAValidUpdateException("Question round has already been issued");
        }

        sessionEventService.createSessionEvent(onlineHearing, EventTypes.QUESTION_ROUND_ISSUED.getEventType());
        questionRoundService.issueQuestionRound(onlineHearing, questionStateOptional.get(), roundId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
