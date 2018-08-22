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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.coh.controller.question.*;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.controller.validators.QuestionValidator;
import uk.gov.hmcts.reform.coh.controller.validators.Validation;
import uk.gov.hmcts.reform.coh.controller.validators.ValidationResult;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.service.QuestionStateService;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

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

    private Validation validation = new Validation();

    private AnswerService answerService;

    private static final String QUESTION_NOT_FOUND = "Question not found";
    private static final String ONLINE_HEARING_NOT_FOUND = "Online hearing not found";

    @Autowired
    public QuestionController(QuestionService questionService, OnlineHearingService onlineHearingService, QuestionStateService questionStateService, AnswerService answerService) {
        this.questionService = questionService;
        this.onlineHearingService = onlineHearingService;
        this.questionStateService = questionStateService;
        this.answerService = answerService;
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
    public ResponseEntity getQuestions(@PathVariable UUID onlineHearingId) {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingId);

        Optional<List<Question>> optionalQuestions = questionService.findAllQuestionsByOnlineHearing(onlineHearing);

        if (!optionalQuestions.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(QUESTION_NOT_FOUND);
        }

        List<Question> questions = optionalQuestions.get();
        List<QuestionResponse> responses = new ArrayList<>();
        for (Question question : questions) {
            List<Answer> answers = answerService.retrieveAnswersByQuestion(question);
            QuestionResponse questionResponse = new QuestionResponse();
            if (answers.isEmpty()) {
                QuestionResponseMapper.map(question, questionResponse);
            } else {
                QuestionResponseMapper.map(question, questionResponse, answers.get(0));
            }
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
    public ResponseEntity getQuestion(@PathVariable UUID questionId) {
        QuestionResponse questionResponse = new QuestionResponse();
        Optional<Question> optionalQuestion = questionService.retrieveQuestionById(questionId);
        if (!optionalQuestion.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(QUESTION_NOT_FOUND);
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
    public ResponseEntity createQuestion(UriComponentsBuilder uriBuilder, @PathVariable UUID onlineHearingId, @RequestBody QuestionRequest request) {

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingId);

        Optional<OnlineHearing> savedOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearing);
        if (!savedOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ONLINE_HEARING_NOT_FOUND);
        }

        ValidationResult result = validation.execute(QuestionValidator.values(), request);
        if (!result.isValid()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(result.getReason());
        }

        Question question = new Question();
        QuestionRequestMapper mapper = new QuestionRequestMapper(question, savedOnlineHearing.get(), request);
        mapper.map();
        question = questionService.createQuestion(question, savedOnlineHearing.get());

        CreateQuestionResponse response = new CreateQuestionResponse();
        response.setQuestionId(question.getQuestionId());

        UriComponents uriComponents =
                uriBuilder.path(CohUriBuilder.buildQuestionGet(onlineHearingId, question.getQuestionId())).build();

        return ResponseEntity.created(uriComponents.toUri()).body(response);
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

        ValidationResult result = validation.execute(QuestionValidator.values(), request);
        if (!result.isValid()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(result.getReason());
        }
        else if (StringUtils.isEmpty(request.getQuestionState())) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Question state is required");
        }

        synchronized (QuestionController.class) {
            // This will block on multiple update attempts.
            Optional<Question> optionalQuestion = questionService.retrieveQuestionById(questionId);
            if (!optionalQuestion.isPresent()) {
                return new ResponseEntity<>(QUESTION_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            Question savedQuestion = optionalQuestion.get();

            if (!savedQuestion.getOnlineHearing().getOnlineHearingId().equals(onlineHearingId)) {
                return new ResponseEntity<>("Online hearing ID does not match question online hearing ID", HttpStatus.BAD_REQUEST);
            }

            Optional<QuestionState> optionalModifiedState = questionStateService.retrieveQuestionStateByStateName(request.getQuestionState());
            if (request.getQuestionState().equals(QuestionStates.ISSUED.getStateName()) || !optionalModifiedState.isPresent()) {
                return new ResponseEntity<>("Not allowed to issue single questions", HttpStatus.UNPROCESSABLE_ENTITY);
            }

            UpdateQuestionRequestMapper.map(savedQuestion, request);

            questionService.updateQuestion(savedQuestion);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @ApiOperation("Delete a question")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @DeleteMapping(value = "/questions/{questionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteQuestion(@PathVariable UUID onlineHearingId, @PathVariable UUID questionId) {

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingId);
        Optional<OnlineHearing> savedOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearing);

        if (!savedOnlineHearing.isPresent()) {
            return new ResponseEntity<>(ONLINE_HEARING_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        synchronized (QuestionController.class) {
            Optional<Question> optionalQuestion = questionService.retrieveQuestionById(questionId);
            if (!optionalQuestion.isPresent()) {
                return ResponseEntity.noContent().build();
            }

            Question question = optionalQuestion.get();
            if (!question.getQuestionState().getState().equals(QuestionStates.DRAFTED.getStateName())) {
                return new ResponseEntity<>("Only drafted questions can be deleted", HttpStatus.UNPROCESSABLE_ENTITY);
            }

            questionService.deleteQuestion(question);
        }

        return ResponseEntity.ok().build();
    }
}
