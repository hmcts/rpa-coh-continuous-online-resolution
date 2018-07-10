package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.coh.controller.question.*;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/continuous-online-hearings/{onlineHearingId}")
public class QuestionController {

    private QuestionService questionService;
    private OnlineHearingService onlineHearingService;

    @Autowired
    public QuestionController(QuestionService questionService, OnlineHearingService onlineHearingService) {
        this.questionService = questionService;
        this.onlineHearingService = onlineHearingService;
    }

    @ApiOperation("Get a question")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = QuestionResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @GetMapping("/questions/{questionId}")
    public ResponseEntity<QuestionResponse> getQuestion(@PathVariable UUID questionId) {
        QuestionResponse questionResponse = new QuestionResponse();
        Optional<Question> optionalQuestion = questionService.retrieveQuestionById(questionId);
        if (!optionalQuestion.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Question question = optionalQuestion.get();
        QuestionResponseMapper.map(question, questionResponse);

        return ResponseEntity.ok(questionResponse);
    }

    @ApiOperation("Add a new question")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", response = CreateQuestionResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PostMapping(value = "/questions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateQuestionResponse> createQuestion(@PathVariable UUID onlineHearingId, @RequestBody QuestionRequest request) {

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingId);
        Optional<OnlineHearing> savedOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearing);

        if (!savedOnlineHearing.isPresent() || !validate(request)) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Question question = new Question();
        QuestionRequestMapper mapper = new QuestionRequestMapper(question, savedOnlineHearing.get(), request);
        mapper.map();
        question = questionService.createQuestion(question, onlineHearingId);

        CreateQuestionResponse response = new CreateQuestionResponse();
        response.setQuestionId(question.getQuestionId());

        return ResponseEntity.ok(response);
    }

    @ApiOperation("Edit a question")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Question.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PatchMapping(value = "/questions/{questionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Question> editQuestion(@PathVariable UUID onlineHearingId, @PathVariable UUID questionId, @RequestBody Question body) {

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingId);
        Optional<OnlineHearing> onlineHearingOptional = onlineHearingService.retrieveOnlineHearing(onlineHearing);
        if(!onlineHearingOptional.isPresent()){
            return new ResponseEntity<Question>(HttpStatus.BAD_REQUEST);
        }

        Optional<Question> optionalQuestion = questionService.retrieveQuestionById(questionId);
        if(!optionalQuestion.isPresent()){
            return new ResponseEntity<Question>(HttpStatus.BAD_REQUEST);
        }
        Question question = optionalQuestion.get();

        if(!question.getOnlineHearing().equals(onlineHearingOptional.get())){
            return new ResponseEntity<Question>(HttpStatus.BAD_REQUEST);
        }

        question = questionService.updateQuestion(question, body);
        return ResponseEntity.ok(question);
    }

    private boolean validate(QuestionRequest request) {

        if (StringUtils.isEmpty(request.getQuestionRound())
                || StringUtils.isEmpty(request.getQuestionOrdinal())
                || StringUtils.isEmpty(request.getQuestionHeaderText())
                || StringUtils.isEmpty(request.getQuestionBodyText())
                || StringUtils.isEmpty(request.getOwnerReference())
                ) {

            return false;
        }

        return true;
    }
}
