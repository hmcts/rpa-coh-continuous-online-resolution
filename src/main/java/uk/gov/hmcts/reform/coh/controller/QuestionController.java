package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.service.QuestionService;

import java.util.UUID;

@RestController
@RequestMapping("/online-hearings/{onlineHearingId}")
public class QuestionController {

    private QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @ApiOperation("Add a new question")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", response = Question.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PostMapping(value = "/questions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Question> createQuestion(@PathVariable UUID onlineHearingId, @RequestBody Question body) {
        return ResponseEntity.ok(questionService.createQuestion(body, onlineHearingId));
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
    public ResponseEntity<Question> editQuestion(@PathVariable Long questionId, @RequestBody Question body) {
        return ResponseEntity.ok(questionService.editQuestion(questionId, body));
    }

    @ApiOperation(value = "Issue Question", notes = "A GET request is used to notify the jurisdiction.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = QuestionRound.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @GetMapping("/questions/{questionId}")
    public ResponseEntity<Question> issueQuestion(@PathVariable Long questionId) {

        Question question = questionService.retrieveQuestionById(questionId);
        if(question == null){
            return new ResponseEntity<Question>(HttpStatus.BAD_REQUEST);
        }

        boolean success = questionService.issueQuestion(question);
        if(success) {
            return ResponseEntity.ok(question);
        }else {
            return new ResponseEntity<Question>(HttpStatus.FAILED_DEPENDENCY);
        }
    }
}
