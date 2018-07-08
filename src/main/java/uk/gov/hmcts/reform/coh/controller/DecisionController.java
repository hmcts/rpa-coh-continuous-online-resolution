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
import uk.gov.hmcts.reform.coh.controller.decision.*;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingMapper;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingResponse;
import uk.gov.hmcts.reform.coh.controller.validators.DecisionRequestValidator;
import uk.gov.hmcts.reform.coh.controller.validators.ValidationResult;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionState;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.service.DecisionService;
import uk.gov.hmcts.reform.coh.service.DecisionStateService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/continuous-online-hearings/{onlineHearingId}/decisions")
public class DecisionController {

    private static final Logger log = LoggerFactory.getLogger(AnswerController.class);

    private static final String STARTING_STATE = "decision_drafted";

    private OnlineHearingService onlineHearingService;

    private DecisionService decisionService;

    private DecisionStateService decisionStateService;

    @Autowired
    public DecisionController(OnlineHearingService onlineHearingService, DecisionService decisionService, DecisionStateService decisionStateService) {
        this.onlineHearingService = onlineHearingService;
        this.decisionService = decisionService;
        this.decisionStateService = decisionStateService;
    }

    @ApiOperation(value = "Create decision", notes = "A POST request is used to create a decision")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Success", response = Decision.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createDecision(@PathVariable UUID onlineHearingId, @RequestBody DecisionRequest request) {

        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearingId);
        if (!optionalOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Online hearing not found");
        }

        ValidationResult result = DecisionRequestValidator.validate(request);
        if (!result.isValid()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(result.getReason());
        }

        Optional<DecisionState> optionalDecisionState = decisionStateService.retrieveDecisionStateByState(STARTING_STATE);
        if (!optionalDecisionState.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Unable to retrieve starting state for decision");
        }

        Decision decision = new Decision();
        DecisionRequestMapper.map(request, decision, optionalDecisionState.get());
        decision = decisionService.createDecision(decision);
        CreateDecisionResponse response = new CreateDecisionResponse();
        response.setDecisionId(decision.getDecisionId());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Get decision", notes = "A GET request to retrieve a decision")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = OnlineHearing.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @GetMapping(value = "{decisionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity retrieveDecision(@PathVariable UUID onlineHearingId, @PathVariable UUID decisionId ) {

        Optional<Decision> optionalDecision = decisionService.retrieveByOnlineHearingIdAndDecisionId(onlineHearingId, decisionId);
        if (!optionalDecision.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unable to find decision");
        }

        DecisionResponse decisionResponse = new DecisionResponse();
        DecisionResponseMapper.map(optionalDecision.get(), decisionResponse);
        return new ResponseEntity<>(decisionResponse, HttpStatus.OK);
    }
}