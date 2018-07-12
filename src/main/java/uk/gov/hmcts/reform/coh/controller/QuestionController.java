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
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.service.QuestionStateService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/continuous-online-hearings/{onlineHearingId}")
public class QuestionController {

    private QuestionService questionService;
    private OnlineHearingService onlineHearingService;
    private QuestionStateService questionStateService;

    @Autowired
    public QuestionController(QuestionService questionService, OnlineHearingService onlineHearingService, QuestionStateService questionStateService) {
        this.questionService = questionService;
        this.onlineHearingService = onlineHearingService;
        this.questionStateService = questionStateService;
    }

    @ApiOperation("Get all questions for an online hearing")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = AllQuestionsResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @GetMapping("/questions")
    public ResponseEntity<AllQuestionsResponse> getQuestions(@PathVariable UUID onlineHearingId) {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingId);

        Optional<List<Question>> optionalQuestions = questionService.finaAllQuestionsByOnlineHearing(onlineHearing);

        if (!optionalQuestions.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Question> questions = optionalQuestions.get();
        List<QuestionResponse> responses = new ArrayList<>();
        for (Question question : questions) {
            QuestionResponse questionResponse = new QuestionResponse();
            QuestionResponseMapper.map(question, questionResponse);
            responses.add(questionResponse);
        }
        AllQuestionsResponse response = new AllQuestionsResponse();
        response.setQuestions(responses);

        return ResponseEntity.ok(response);
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
        question = questionService.createQuestion(question, savedOnlineHearing.get());

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
    @PutMapping(value = "/questions/{questionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity editQuestion(@PathVariable UUID onlineHearingId, @PathVariable UUID questionId,
                                                 @RequestBody UpdateQuestionRequest request) {

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingId);
        Optional<OnlineHearing> onlineHearingOptional = onlineHearingService.retrieveOnlineHearing(onlineHearing);
        if(!onlineHearingOptional.isPresent()){
            return new ResponseEntity<>("Online hearing not found", HttpStatus.NOT_FOUND);
        }

        Optional<Question> optionalQuestion = questionService.retrieveQuestionById(questionId);
        if(!optionalQuestion.isPresent()){
            return new ResponseEntity<>("Question not found", HttpStatus.NOT_FOUND);
        }
        Question savedQuestion = optionalQuestion.get();

        if(!savedQuestion.getOnlineHearing().equals(onlineHearingOptional.get())){
            return new ResponseEntity<>("Online hearing ID does not match question online hearing ID", HttpStatus.BAD_REQUEST);
        }

        Optional<QuestionState> optionalQuestionState = questionStateService.retrieveQuestionStateByStateName(request.getQuestionState());

        if(request.getQuestionState().equals("ISSUED") || !optionalQuestionState.isPresent()) {
            return new ResponseEntity<>("Question state not found", HttpStatus.BAD_REQUEST);
        }

        UpdateQuestionRequestMapper.map(savedQuestion, request);
        savedQuestion.setQuestionState(optionalQuestionState.get());

        questionService.updateQuestion(savedQuestion);
        return new ResponseEntity<>(HttpStatus.OK);
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
