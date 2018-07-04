package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundResponse;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundResponseMapper;
import uk.gov.hmcts.reform.coh.controller.questionrounds.AllQuestionRoundsResponse;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.domain.QuestionRoundState;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionRoundService;
import uk.gov.hmcts.reform.coh.service.QuestionService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/online-hearings/{onlineHearingId}")
public class QuestionRoundController {

    @Autowired
    private QuestionRoundService questionRoundService;

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private QuestionService questionService;


    @ApiOperation("Get a question round")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Question.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @GetMapping("/questionrounds")
    public ResponseEntity<AllQuestionRoundsResponse> getQuestionRounds(@PathVariable UUID onlineHearingId) {

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingId);
        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearing);
        if(!optionalOnlineHearing.isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        onlineHearing = optionalOnlineHearing.get();
        List<QuestionRound> questionRounds = questionRoundService.getAllQuestionRounds(onlineHearing);

        AllQuestionRoundsResponse allQuestionRoundsResponse = new AllQuestionRoundsResponse();

        QuestionRoundState questionRoundState = new QuestionRoundState();
        questionRoundState.setState("Issued");
        for(QuestionRound questionRound : questionRounds) {
            QuestionRoundResponse questionRoundResponse = new QuestionRoundResponse();
            questionRound.setQuestionRoundState(questionRoundState);
            QuestionRoundResponseMapper.map(questionRound, questionRoundResponse);
            allQuestionRoundsResponse.addQuestionRoundResponse(questionRoundResponse);
        }

        allQuestionRoundsResponse.setCurrentQuestionRound(questionRoundService.getQuestionRoundNumber(onlineHearing));
        allQuestionRoundsResponse.setNextQuestionRound(questionRoundService.getNextQuestionRound(onlineHearing, allQuestionRoundsResponse.getCurrentQuestionRound()));
        allQuestionRoundsResponse.setMaxQuestionRound(onlineHearing.getJurisdiction().getMaxQuestionRounds().get());
        allQuestionRoundsResponse.setPreviousQuestionRound(questionRoundService.getPreviousQuestionRound(allQuestionRoundsResponse.getCurrentQuestionRound()));
        return ResponseEntity.ok(allQuestionRoundsResponse);
    }
}
