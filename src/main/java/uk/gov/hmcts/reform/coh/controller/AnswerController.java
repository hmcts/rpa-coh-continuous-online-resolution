package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerRequest;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.AnswerState;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.AnswerStateService;
import uk.gov.hmcts.reform.coh.service.QuestionService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/online-hearings/{onlineHearingId}/questions/{questionId}/answers")
public class AnswerController {

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
            @ApiResponse(code = 422, message = "Validator error")
    })
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnswerResponse> createAnswer(@PathVariable Long onlineHearingId, @PathVariable Long questionId, @RequestBody AnswerRequest request) {

        ValidationResult validationResult = validate(request);
        if (!validationResult.isValid()) {
            return new ResponseEntity<AnswerResponse>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        AnswerResponse answerResponse = new AnswerResponse();
        try {
            Answer answer = new Answer();
            Question question = questionService.retrieveQuestionById(questionId);

            if (question == null) {
                return new ResponseEntity<AnswerResponse>(HttpStatus.FAILED_DEPENDENCY);
            }

            answer.setAnswerText(request.getAnswerText());
            answer.setQuestion(question);
            answer = answerService.createAnswer(answer);
            answerResponse.setAnswerId(answer.getAnswerId());
        } catch (Exception e) {
            System.out.println("Exception in createAnswer: " + e.getMessage());
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
    public ResponseEntity<Answer> retrieveAnswer(@PathVariable long answerId) {

        Optional<Answer> answer = answerService.retrieveAnswerById(answerId);
        if (!answer.isPresent()) {
            return new ResponseEntity<Answer>(HttpStatus.NOT_FOUND);
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
    public ResponseEntity<List<Answer>> retrieveAnswers(@PathVariable Long questionId) {

        // Nothing to return if question doesn't exist
        Question question = questionService.retrieveQuestionById(questionId);
        if (question == null) {
            return new ResponseEntity<List<Answer>>(HttpStatus.FAILED_DEPENDENCY);
        }

        List<Answer> answers = answerService.retrieveAnswersByQuestion(question);

        return ResponseEntity.ok(answers);
    }

    @ApiOperation(value = "Update Answers", notes = "A PATCH request is used to update an answer")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Answer.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validator error")
    })
    @PatchMapping(value = "{answerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnswerResponse> updateAnswer(@PathVariable Long questionId, @PathVariable long answerId, @RequestBody AnswerRequest request) {

        AnswerResponse answerResponse = new AnswerResponse();
        try {
            Question question = new Question();
            question.setQuestionId(questionId);
            Answer answer = new Answer().answerId(answerId)
                    .answerText(request.getAnswerText());
            answer.setQuestion(question);

            Optional<Answer> optionalAnswer = answerService.retrieveAnswerById(answerId);
            if (!optionalAnswer.isPresent()) {
                return new ResponseEntity<AnswerResponse>(HttpStatus.NOT_FOUND);
            }

            answerService.updateAnswerById(answer);
            answerResponse.setAnswerId(answer.getAnswerId());
        } catch (Exception e) {
            return new ResponseEntity<AnswerResponse>(HttpStatus.FAILED_DEPENDENCY);
        }

        return ResponseEntity.ok(answerResponse);
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

    private class ValidationResult {

        private boolean isValid;
        private String reason;

        public boolean isValid() {
            return isValid;
        }

        public void setValid(boolean valid) {
            isValid = valid;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
