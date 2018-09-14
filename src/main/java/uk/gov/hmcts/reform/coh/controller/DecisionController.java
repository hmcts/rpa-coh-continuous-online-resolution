package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.coh.controller.decision.*;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.*;
import uk.gov.hmcts.reform.coh.controller.utils.AuthUtils;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.controller.validators.DecisionRequestValidator;
import uk.gov.hmcts.reform.coh.controller.validators.Validation;
import uk.gov.hmcts.reform.coh.controller.validators.ValidationResult;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionReply;
import uk.gov.hmcts.reform.coh.domain.DecisionState;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.exception.GenericException;
import uk.gov.hmcts.reform.coh.service.*;
import uk.gov.hmcts.reform.coh.service.utils.ExpiryCalendar;
import uk.gov.hmcts.reform.coh.states.DecisionsStates;
import uk.gov.hmcts.reform.logging.exception.AlertLevel;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.coh.controller.utils.CommonMessages.ONLINE_HEARING_NOT_FOUND;
import static uk.gov.hmcts.reform.coh.handlers.IdamHeaderInterceptor.IDAM_AUTHORIZATION;

@RestController
@RequestMapping("/continuous-online-hearings/{onlineHearingId}")
public class DecisionController {

    private static final Logger log = LoggerFactory.getLogger(AnswerController.class);

    private static final String STARTING_STATE = DecisionsStates.DECISION_DRAFTED.getStateName();

    private static final String PENDING_STATE = DecisionsStates.DECISION_ISSUE_PENDING.getStateName();
    private static final String UNABLE_TO_FIND_DECISION = "Unable to find decision";
    private static final String MISSING_AUTHOR_MESSAGE = "Authorization author id must not be empty";

    private OnlineHearingService onlineHearingService;
    private DecisionService decisionService;
    private DecisionStateService decisionStateService;
    private SessionEventService sessionEventService;
    private DecisionReplyService decisionReplyService;

    private Validation validation = new Validation();

    @Autowired
    public DecisionController(OnlineHearingService onlineHearingService, DecisionService decisionService,
                              DecisionStateService decisionStateService, SessionEventService sessionEventService,
                              DecisionReplyService decisionReplyService) {
        this.onlineHearingService = onlineHearingService;
        this.decisionService = decisionService;
        this.decisionStateService = decisionStateService;
        this.sessionEventService = sessionEventService;
        this.decisionReplyService = decisionReplyService;
    }

    @ApiOperation(value = "Create decision", notes = "A POST request is used to create a decision")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Success", response = CreateDecisionResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Online hearing not found"),
            @ApiResponse(code = 409, message = "Online hearing already contains a decision"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PostMapping(value = "/decisions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createDecision(UriComponentsBuilder uriBuilder,
                                         @RequestHeader(value=IDAM_AUTHORIZATION) String authorReferenceId,
                                         @PathVariable UUID onlineHearingId, @RequestBody DecisionRequest request) {

        if(authorReferenceId == null || authorReferenceId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MISSING_AUTHOR_MESSAGE);
        }

        Optional<Decision> optionalDecision = decisionService.findByOnlineHearingId(onlineHearingId);
        if (optionalDecision.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Online hearing already contains a decision");
        }

        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearingId);
        if (!optionalOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ONLINE_HEARING_NOT_FOUND);
        }

        ValidationResult result = validation.execute(DecisionRequestValidator.values(), request);
        if (!result.isValid()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(result.getReason());
        }

        Optional<DecisionState> optionalDecisionState = decisionStateService.retrieveDecisionStateByState(STARTING_STATE);
        if (!optionalDecisionState.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Unable to retrieve starting state for decision");
        }

        Decision decision = new Decision();
        decision.setOnlineHearing(optionalOnlineHearing.get());
        DecisionRequestMapper.map(request, decision, optionalDecisionState.get());
        decision.addDecisionStateHistory(optionalDecisionState.get());
        AuthUtils.withIdentity(username -> {
            decision.setAuthorReferenceId(username);
            decision.setOwnerReferenceId(username);
        });
        Decision storedDecision = decisionService.createDecision(decision);

        UriComponents uriComponents =
                uriBuilder.path(CohUriBuilder.buildDecisionGet(onlineHearingId)).build();

        return ResponseEntity.created(uriComponents.toUri()).body(new CreateDecisionResponse(storedDecision.getDecisionId()));
    }

    @ApiOperation(value = "Get decision", notes = "A GET request to retrieve a decision")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = DecisionResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @GetMapping(value = "/decisions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity retrieveDecision(@PathVariable UUID onlineHearingId) {

        Optional<Decision> optionalDecision = decisionService.findByOnlineHearingId(onlineHearingId);
        if (!optionalDecision.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(UNABLE_TO_FIND_DECISION);
        }

        DecisionResponse decisionResponse = new DecisionResponse();
        DecisionResponseMapper.map(optionalDecision.get(), decisionResponse);
        return new ResponseEntity<>(decisionResponse, HttpStatus.OK);
    }

    @ApiOperation(value = "Update a decision", notes = "A PUT request to update replace a decision")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 409, message = "Conflict"),
            @ApiResponse(code = 422, message = "Validation Error")
    })
    @PutMapping(value = "/decisions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateDecision(@RequestHeader(value=IDAM_AUTHORIZATION) String authorReferenceId,
                                         @PathVariable UUID onlineHearingId, @RequestBody UpdateDecisionRequest request) {

        if(authorReferenceId == null || authorReferenceId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MISSING_AUTHOR_MESSAGE);
        }

        Optional<Decision> optionalDecision = decisionService.findByOnlineHearingId(onlineHearingId);
        if (!optionalDecision.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(UNABLE_TO_FIND_DECISION);
        }

        // Only draft decisions can be updated
        Decision decision = optionalDecision.get();
        if (!decision.getDecisionstate().getState().equals(DecisionsStates.DECISION_DRAFTED.getStateName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Only draft decisions can be updated");
        }

        // Check the stated passed in the request
        Optional<DecisionState> optionalDecisionState = decisionStateService.retrieveDecisionStateByState(request.getState());
        if (!optionalDecisionState.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Invalid state");
        }

        // check that the state is 'decision_issue_pending' or 'decision_drafted'
        if (!request.getState().equals(DecisionsStates.DECISION_ISSUE_PENDING.getStateName())&&!request.getState().equals(DecisionsStates.DECISION_DRAFTED.getStateName())){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Invalid state");
        } else {
            // If a decision is issued, then there is a deadline to accept or reject it
            decision.setDeadlineExpiryDate(ExpiryCalendar.getInstance().getDeadlineExpiryDate());
        }

        // The remaining validation is same as DecisionRequest for create
        ValidationResult result = validation.execute(DecisionRequestValidator.values(), request);
        if (!result.isValid()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(result.getReason());
        }

        // Update the decision
        decision.addDecisionStateHistory(optionalDecisionState.get());
        DecisionRequestMapper.map(request, decision, optionalDecisionState.get());
        AuthUtils.withIdentity(username -> {
            decision.setAuthorReferenceId(username);
            decision.setOwnerReferenceId(username);
        });
        decisionService.updateDecision(decision);

        try {
            // Now queue the notification
            queueDecisionIssue(decision);
        } catch (Exception e) {
            log.error(
                "Unable to create a session event to for " + EventTypes.DECISION_ISSUED.getEventType(),
                new GenericException(AlertLevel.P2, e)
            );
        }

        return ResponseEntity.ok("");
    }

    private void queueDecisionIssue(Decision decision) {

        Optional<DecisionState> pendingState = decisionStateService.retrieveDecisionStateByState(PENDING_STATE);
        if (pendingState.isPresent() && decision.getDecisionstate().equals(pendingState.get())) {
            sessionEventService.createSessionEvent(decision.getOnlineHearing(), EventTypes.DECISION_ISSUED.getEventType());
        }
    }

    @ApiOperation(value = "Reply to a decision", notes = "A POST request is used to reply to a decision")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Success", response = CreateDecisionReplyResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Online hearing not found")
    })
    @PostMapping(value = "/decisionreplies", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity replyToDecision(UriComponentsBuilder uriBuilder,
                                          @RequestHeader(value=IDAM_AUTHORIZATION) String authorReferenceId,
                                          @PathVariable UUID onlineHearingId, @Valid @RequestBody DecisionReplyRequest request) {

        if(authorReferenceId == null || authorReferenceId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MISSING_AUTHOR_MESSAGE);
        }

        if(!request.getDecisionReply().equalsIgnoreCase(DecisionsStates.DECISIONS_ACCEPTED.getStateName())
            && !request.getDecisionReply().equalsIgnoreCase(DecisionsStates.DECISIONS_REJECTED.getStateName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Decision reply field is not valid");
        }

        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearingId);
        if (!optionalOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ONLINE_HEARING_NOT_FOUND);
        }

        Optional<Decision> optionalDecision = decisionService.findByOnlineHearingId(onlineHearingId);
        if (!optionalDecision.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(UNABLE_TO_FIND_DECISION);
        }

        Decision decision = optionalDecision.get();
        if(!decision.getDecisionstate().getState().equalsIgnoreCase(DecisionsStates.DECISION_ISSUED.getStateName())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Decision must be issued before replying");
        }

        DecisionReply decisionReply = new DecisionReply();
        DecisionReplyRequestMapper.map(request, decisionReply, decision, authorReferenceId);
        decisionReply.setDateOccured(new Date());
        decisionReply = decisionReplyService.createDecision(decisionReply);

        UriComponents uriComponents =
                uriBuilder.path(CohUriBuilder.buildDecisionReplyGet(onlineHearingId, decisionReply.getId())).build();

        return ResponseEntity.created(uriComponents.toUri()).body(new CreateDecisionReplyResponse(decisionReply.getId()));
    }

    @ApiOperation(value = "Get all replies to decision", notes = "A GET request is used to get all replies to a decision")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = AllDecisionRepliesResponse.class),
            @ApiResponse(code = 404, message = "Online hearing not found")
    })
    @GetMapping(value = "/decisionreplies", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllDecisionReplies(@PathVariable UUID onlineHearingId) {

        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearingId);
        if (!optionalOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ONLINE_HEARING_NOT_FOUND);
        }

        Optional<Decision> optionalDecision = decisionService.findByOnlineHearingId(onlineHearingId);
        if (!optionalDecision.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(UNABLE_TO_FIND_DECISION);
        }

        List<DecisionReply> decisionReplies = decisionReplyService.findAllDecisionReplyByDecision(optionalDecision.get());

        AllDecisionRepliesResponse allDecisionRepliesResponse = new AllDecisionRepliesResponse();
        for(DecisionReply decisionReply : decisionReplies) {
            DecisionReplyResponse replyResponse = new DecisionReplyResponse();
            DecisionReplyResponseMapper.map(decisionReply, replyResponse);
            allDecisionRepliesResponse.addDecisionReply(replyResponse);
        }
        return ResponseEntity.ok().body(allDecisionRepliesResponse);
    }

    @ApiOperation(value = "Get a replies to decision", notes = "A GET request is used to get a reply to a decision")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = DecisionReplyResponse.class),
            @ApiResponse(code = 404, message = "Online hearing not found")
    })
    @GetMapping(value = "/decisionreplies/{decisionReplyId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getADecisionReply(@PathVariable UUID onlineHearingId, @PathVariable UUID decisionReplyId) {

        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearingId);
        if (!optionalOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ONLINE_HEARING_NOT_FOUND);
        }

        Optional<Decision> optionalDecision = decisionService.findByOnlineHearingId(onlineHearingId);
        if (!optionalDecision.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(UNABLE_TO_FIND_DECISION);
        }

        Optional<DecisionReply> optionalDecisionReply = decisionReplyService.findByDecisionReplyId(decisionReplyId);
        if(!optionalDecisionReply.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unable to find decision reply");
        }

        DecisionReplyResponse decisionReplyResponse = new DecisionReplyResponse();
        DecisionReplyResponseMapper.map(optionalDecisionReply.get(), decisionReplyResponse);

        return ResponseEntity.ok().body(decisionReplyResponse);
    }
}