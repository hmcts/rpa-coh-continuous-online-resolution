package uk.gov.hmcts.reform.coh.controller;

import com.google.common.collect.ImmutableMap;

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
import uk.gov.hmcts.reform.coh.appinsights.EventRepository;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerRequest;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponseMapper;
import uk.gov.hmcts.reform.coh.controller.answer.CreateAnswerResponse;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.controller.validators.ValidationResult;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.exception.GenericException;
import uk.gov.hmcts.reform.coh.service.*;
import uk.gov.hmcts.reform.coh.states.AnswerStates;
import uk.gov.hmcts.reform.coh.states.OnlineHearingStates;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.coh.controller.utils.CommonMessages.ONLINE_HEARING_NOT_FOUND;

@RestController
@RequestMapping("/continuous-online-hearings/{onlineHearingId}/questions/{questionId}/answers")
public class AnswerController {
    private static final Logger log = LoggerFactory.getLogger(AnswerController.class);

    @Autowired
    private AnswerService answerService;

    @Autowired
    private AnswerStateService answerStateService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionStateService questionStateService;

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private SessionEventService sessionEventService;

    @Autowired
    private EventRepository eventRepository;

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

        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearingId);
        if (!optionalOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ONLINE_HEARING_NOT_FOUND);
        }

        // Done like this to satisfy Sonar. This needs to be refactored.
        Optional<AnswerState> answerState = answerStateService.retrieveAnswerStateByState(request.getAnswerState());
        if(!answerState.isPresent()){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(String.format("Answer state is not valid: %s", request.getAnswerState()));
        }

        ValidationResult validationResult = validate(optionalOnlineHearing.get(), request);
        if (!validationResult.isValid()) {
            return ResponseEntity.unprocessableEntity().body(validationResult.getReason());
        }

        CreateAnswerResponse answerResponse = new CreateAnswerResponse();
        Optional<Question> optionalQuestion = questionService.retrieveQuestionById(questionId);
        try {

            List<QuestionStates> answerableStates
                = Arrays.asList(QuestionStates.ISSUED, QuestionStates.QUESTION_DEADLINE_EXTENSION_GRANTED);

            if (!optionalQuestion.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The question does not exist");
            }

            // For MVP, there'll only be one answer per question
            List<Answer> answers = answerService.retrieveAnswersByQuestion(optionalQuestion.get());
            if (!answers.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Question already has an answer");
            }

            // If a question exists, then it must be in the issued state to be answered
            if (answerableStates.stream().noneMatch(questionStates
                -> optionalQuestion.get().getQuestionState().getState().equals(questionStates.getStateName()))) {

                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("The question can only be answers if it's been issued or a deadline extension has been granted");
            }

            Answer answer = new Answer();
            if (answerState.isPresent()) {
                answer.setAnswerState(answerState.get());
                answer.registerStateChange();
            }

            answer.setAnswerText(request.getAnswerText());
            answer.setQuestion(optionalQuestion.get());
            answerService.createAnswer(answer);
            answerResponse.setAnswerId(answer.getAnswerId());

            performQuestionAnswered(optionalOnlineHearing.get(), answer);

        } catch (Exception e) {
            log.error("Could not create answer", new GenericException(e));
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(e.getMessage());
        }

        UriComponents uriComponents =
                uriBuilder.path(CohUriBuilder.buildAnswerGet(optionalOnlineHearing.get().getOnlineHearingId(),
                        optionalQuestion.get().getQuestionId(), answerResponse.getAnswerId())).build();

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

        AnswerResponse response = new AnswerResponse();
        AnswerResponseMapper.map(answer.get(), response);

        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Get Answers", notes = "A GET request without a body is used to retrieve all answers to a question")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = AnswerResponse.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @GetMapping(value = "")
    public ResponseEntity retrieveAnswers(@PathVariable UUID questionId) {

        // Nothing to return if question doesn't exist
        Optional<Question> optionalQuestion = questionService.retrieveQuestionById(questionId);
        if (!optionalQuestion.isPresent()) {
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body("Question not found");
        }

        Question question = optionalQuestion.get();
        List<Answer> answers = answerService.retrieveAnswersByQuestion(question);
        List<AnswerResponse> responses = answers
                .stream()
                .map(a ->
                        {
                            AnswerResponse response = new AnswerResponse();
                            AnswerResponseMapper.map(a, response);
                            return response;
                        }
                    )
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @ApiOperation(value = "Update Answers", notes = "A PUT request is used to update an answer")
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
            eventRepository.trackEvent("Online hearing not found", ImmutableMap.of(
                "onlineHearingId", onlineHearingId.toString()
            ));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ONLINE_HEARING_NOT_FOUND);
        }

        Optional<AnswerState> optionalAnswerState = answerStateService.retrieveAnswerStateByState(request.getAnswerState());
        if(!optionalAnswerState.isPresent()){
            eventRepository.trackEvent("Invalid answer state", ImmutableMap.of(
                    "onlineHearingId", onlineHearingId.toString(),
                    "requestedState", request.getAnswerState()
            ));
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(String.format("Answer state is not valid: %s", request.getAnswerState()));
        }

        ValidationResult validationResult = validate(optionalOnlineHearing.get(), request);
        if (!validationResult.isValid()) {
            eventRepository.trackEvent("Invalid answer request", ImmutableMap.of(
                "onlineHearingId", onlineHearingId.toString(),
                "reason", validationResult.getReason()
            ));
            return ResponseEntity.unprocessableEntity().body(validationResult.getReason());
        }

        Optional<Answer> optAnswer = answerService.retrieveAnswerById(answerId);
        if(!optAnswer.isPresent()){
            eventRepository.trackEvent("Answer does not exist", ImmutableMap.of(
                "onlineHearingId", onlineHearingId.toString(),
                "requestedAnswerId", answerId.toString()
            ));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Answer does not exist");
        }

        // Submitted answers cannot be updated
        Answer savedAnswer = optAnswer.get();
        if(savedAnswer.getAnswerState().getState().equals(AnswerStates.SUBMITTED.getStateName())){
            eventRepository.trackEvent("Submitted answers cannot be updated", ImmutableMap.of(
                "onlineHearingId", onlineHearingId.toString(),
                "answerId", answerId.toString(),
                "state", savedAnswer.getAnswerState().getState(),
                "requestedState", request.getAnswerState()
            ));
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Submitted answers cannot be updated");
        }

        Answer body = new Answer();
        body.setAnswerState(optionalAnswerState.get());
        body.setAnswerText(request.getAnswerText());

        try {
            Answer updatedAnswer = answerService.updateAnswer(optAnswer.get(), body);
            CreateAnswerResponse answerResponse = new CreateAnswerResponse();
            answerResponse.setAnswerId(updatedAnswer.getAnswerId());

            performQuestionAnswered(optionalOnlineHearing.get(), updatedAnswer);

            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            eventRepository.trackEvent("Not found answer error", ImmutableMap.of(
                "onlineHearingId", onlineHearingId.toString(),
                "answerId", answerId.toString(),
                "requestedState", request.getAnswerState(),
                "error", e.getMessage()
            ));
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
        }
    }

    private void performQuestionAnswered(OnlineHearing onlineHearing, Answer answer) {

        if (answer.getAnswerState().getState().equals(AnswerStates.SUBMITTED.getStateName())) {

            try {
                // Update the state of the question
                QuestionState answeredState = questionStateService.fetchQuestionState(QuestionStates.ANSWERED);
                Question question = answer.getQuestion();
                question.updateQuestionStateHistory(answeredState);
                question.setQuestionState(answeredState);
                questionService.updateQuestionForced(question);
            } catch (Exception e) {
                log.error("Could not get question state", new GenericException(e));
            }
            sessionEventService.createSessionEvent(onlineHearing, EventTypes.ANSWERS_SUBMITTED.getEventType());
        }
    }

    public ValidationResult validate(OnlineHearing onlineHearing, AnswerRequest request) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        if (!onlineHearing.getOnlineHearingState().getState().equalsIgnoreCase(OnlineHearingStates.STARTED.getStateName())) {
            result.setValid(false);
            result.setReason("Answers cannot be submitted after the online hearing a decision has been issued or re-listed");
        } else if (StringUtils.isAllBlank(request.getAnswerText())) {
            result.setValid(false);
            result.setReason("Answer text cannot be empty");
        } else if (StringUtils.isEmpty(request.getAnswerState())) {
            result.setValid(false);
            result.setReason("Answer state cannot be empty");
        } else {
            Optional<AnswerState> optAnswerState = answerStateService.retrieveAnswerStateByState(request.getAnswerState());
            if (!optAnswerState.isPresent()) {
                result.setValid(false);
                result.setReason(String.format("Answer state is not valid: %s", request.getAnswerState()));
            }
        }
        return result;
    }
}
