package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javassist.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerRequest;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.controller.validators.ValidationResult;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.AnswerState;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.AnswerStateService;
import uk.gov.hmcts.reform.coh.service.QuestionService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/continuous-online-hearings/{onlineHearingId}/questions/{questionId}/answers")
public class AnswerController {
    private static final Logger log = LoggerFactory.getLogger(AnswerController.class);

    private AnswerService answerService;

    private AnswerStateService answerStateService;

    private QuestionService questionService;

    @Autowired
    public AnswerController(AnswerService answerService, AnswerStateService answerStateService, QuestionService questionService) {
        this.answerService = answerService;
        this.answerStateService = answerStateService;
        this.questionService = questionService;
    }

    @ApiOperation(value = "Add Answer", notes = "A POST request is used to create an answer")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = AnswerResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnswerResponse> createAnswer(@PathVariable UUID onlineHearingId, @PathVariable UUID questionId, @RequestBody AnswerRequest request) {

        ValidationResult validationResult = validate(request);
        if (!validationResult.isValid()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        AnswerResponse answerResponse = new AnswerResponse();
        try {
            Answer answer = new Answer();
            Optional<Question> optionalQuestion = questionService.retrieveQuestionById(questionId);

            if (!optionalQuestion.isPresent()) {
                return new ResponseEntity<>(HttpStatus.FAILED_DEPENDENCY);
            }

            Optional<AnswerState> answerState = answerStateService.retrieveAnswerStateByState(request.getAnswerState());
            if (answerState.isPresent()) {
                answer.setAnswerState(answerState.get());
                answer.registerStateChange();
            }

            answer.setAnswerText(request.getAnswerText());
            answer.setQuestion(optionalQuestion.get());
            answer = answerService.createAnswer(answer);
            answerResponse.setAnswerId(answer.getAnswerId());
        } catch (Exception e) {
            log.error("Exception in createAnswer: " + e.getMessage());
            return new ResponseEntity<AnswerResponse>(HttpStatus.FAILED_DEPENDENCY);
        }

        return ResponseEntity.ok(answerResponse);
    }

    @ApiOperation(value = "Get Answer", notes = "A GET request with a request body is used to retrieve an answer")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Answer.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @GetMapping(value = "{answerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Answer> retrieveAnswer(@PathVariable UUID answerId) {

        Optional<Answer> answer = answerService.retrieveAnswerById(answerId);
        if (!answer.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(answer.get());
    }

    @ApiOperation(value = "Get Answers", notes = "A GET request without a body is used to retrieve all answers to a question")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Answer.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @GetMapping(value = "")
    public ResponseEntity<List<Answer>> retrieveAnswers(@PathVariable UUID questionId) {

        // Nothing to return if question doesn't exist
        Optional<Question> optionalQuestion = questionService.retrieveQuestionById(questionId);
        if (!optionalQuestion.isPresent()) {
            return new ResponseEntity<List<Answer>>(HttpStatus.FAILED_DEPENDENCY);
        }

        Question question = optionalQuestion.get();
        List<Answer> answers = answerService.retrieveAnswersByQuestion(question);

        return ResponseEntity.ok(answers);
    }

    @ApiOperation(value = "Update Answers", notes = "A PATCH request is used to update an answer")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Answer.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PatchMapping(value = "{answerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnswerResponse> updateAnswer(@PathVariable UUID questionId, @PathVariable UUID answerId, @RequestBody AnswerRequest request) {

        ValidationResult validationResult = validate(request);
        if (!validationResult.isValid()) {
            return new ResponseEntity<AnswerResponse>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<Answer> optAnswer = answerService.retrieveAnswerById(answerId);

        if(!optAnswer.isPresent()){
            return new ResponseEntity<AnswerResponse>(HttpStatus.NOT_FOUND);
        }

        Optional<AnswerState> optionalAnswerState = answerStateService.retrieveAnswerStateByState(request.getAnswerState());
        if(!optionalAnswerState.isPresent()){
            return new ResponseEntity<AnswerResponse>(HttpStatus.FAILED_DEPENDENCY);
        }

        Answer body = new Answer();
        body.setAnswerState(optionalAnswerState.get());
        body.setAnswerText(request.getAnswerText());

        try {
            Answer updatedAnswer = answerService.updateAnswer(optAnswer.get(), body);
            AnswerResponse answerResponse = new AnswerResponse();
            answerResponse.setAnswerId(updatedAnswer.getAnswerId());
            return ResponseEntity.ok(answerResponse);
        } catch (NotFoundException e) {
            return new ResponseEntity<AnswerResponse>(HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private ValidationResult validate(AnswerRequest request) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        if (request.getAnswerText() == null || StringUtils.isEmpty(request.getAnswerText())) {
            result.setValid(false);
            result.setReason("Answer text cannot be empty");
        } else if (StringUtils.isEmpty(request.getAnswerState())) {
            result.setValid(false);
            result.setReason("Answer state cannot be empty");
        } else {
            Optional<AnswerState> optAnswerState = answerStateService.retrieveAnswerStateByState(request.getAnswerState());
            if (!optAnswerState.isPresent()) {
                result.setValid(false);
                result.setReason("Answer state is not valid");
            }
        }
        return result;
    }
}
