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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerRequest;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponseMapper;
import uk.gov.hmcts.reform.coh.controller.answer.CreateAnswerResponse;
import uk.gov.hmcts.reform.coh.controller.validators.ValidationResult;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.AnswerState;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.AnswerStateService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.states.AnswerStates;
import uk.gov.hmcts.reform.coh.states.QuestionStates;
import uk.gov.hmcts.reform.coh.task.AnswersReceivedTask;

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

    private OnlineHearingService onlineHearingService;

    private AnswersReceivedTask answersReceivedTask;

    @Autowired
    public AnswerController(AnswerService answerService, AnswerStateService answerStateService, QuestionService questionService, OnlineHearingService onlineHearingService, AnswersReceivedTask answersReceivedTask) {
        this.answerService = answerService;
        this.answerStateService = answerStateService;
        this.questionService = questionService;
        this.onlineHearingService = onlineHearingService;
        this.answersReceivedTask = answersReceivedTask;
    }

    @ApiOperation(value = "Add Answer", notes = "A POST request is used to create an answer")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = CreateAnswerResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 409, message = "Question already contains an answer"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createAnswer(UriComponentsBuilder uriBuilder, @PathVariable UUID onlineHearingId, @PathVariable UUID questionId, @RequestBody AnswerRequest request) {

        ValidationResult validationResult = validate(request);
        if (!validationResult.isValid()) {
            return ResponseEntity.unprocessableEntity().body(validationResult.getReason());
        }

        CreateAnswerResponse answerResponse = new CreateAnswerResponse();
        try {
            Optional<Question> optionalQuestion = questionService.retrieveQuestionById(questionId);
            // If a question exists, then it must be in the issues state to be answered
            if (!optionalQuestion.isPresent()
                    || !optionalQuestion.get().getQuestionState().getState().equals(QuestionStates.ISSUED.getStateName())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The question does not exist");
            }

            // For MVP, there'll only be one answer per question
            List<Answer> answers = answerService.retrieveAnswersByQuestion(optionalQuestion.get());
            if (!answers.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Question already has an answer");
            }

            Answer answer = new Answer();
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
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(e.getMessage());
        }

        UriComponents uriComponents =
                uriBuilder.path("/continuous-online-hearings/{onlineHearingId}/questions/{questionId}/answers/{answerId}").buildAndExpand(onlineHearingId, questionId, answerResponse.getAnswerId());

        return ResponseEntity.created(uriComponents.toUri()).body(answerResponse);
    }

    @ApiOperation(value = "Get Answer", notes = "A GET request with a request body is used to retrieve an answer")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = AnswerResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @GetMapping(value = "{answerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity retrieveAnswer(@PathVariable UUID answerId) {

        Optional<Answer> answer = answerService.retrieveAnswerById(answerId);
        if (!answer.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Answer not found");
        }
        System.out.println("State: " + answer.get().getAnswerState());
        AnswerResponse response = new AnswerResponse();
        AnswerResponseMapper.map(answer.get(), response);

        return ResponseEntity.ok(response);
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
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PutMapping(value = "{answerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateAnswer(@PathVariable UUID onlineHearingId, @PathVariable UUID questionId, @PathVariable UUID answerId, @RequestBody AnswerRequest request) {

        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearingId);
        if (!optionalOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Online hearing not found");
        }

        ValidationResult validationResult = validate(request);
        if (!validationResult.isValid()) {
            return ResponseEntity.unprocessableEntity().body(validationResult.getReason());
        }

        Optional<AnswerState> optionalAnswerState = answerStateService.retrieveAnswerStateByState(request.getAnswerState());
        if(!optionalAnswerState.isPresent()){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Invalid answer state");
        }

        Optional<Answer> optAnswer = answerService.retrieveAnswerById(answerId);
        if(!optAnswer.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Answer does not exist");
        }

        // Submitted answers cannot be updated
        Answer savedAnswer = optAnswer.get();
        if(savedAnswer.getAnswerState().getState().equals(AnswerStates.SUBMITTED.getStateName())){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Submitted answers cannot be updated");
        }

        Answer body = new Answer();
        body.setAnswerState(optionalAnswerState.get());
        body.setAnswerText(request.getAnswerText());

        try {
            Answer updatedAnswer = answerService.updateAnswer(optAnswer.get(), body);
            CreateAnswerResponse answerResponse = new CreateAnswerResponse();
            answerResponse.setAnswerId(updatedAnswer.getAnswerId());

            answersReceivedTask.execute(optionalOnlineHearing.get());
            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
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
