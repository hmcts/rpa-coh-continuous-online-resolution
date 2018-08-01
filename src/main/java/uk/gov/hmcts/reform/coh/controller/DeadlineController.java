package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.service.exceptions.NoQuestionsAsked;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/continuous-online-hearings/{onlineHearingId}")
public class DeadlineController {

    private static final Logger log = LoggerFactory.getLogger(DeadlineController.class);

    private OnlineHearingService onlineHearingService;
    private QuestionService questionService;

    @Autowired
    public DeadlineController(
        OnlineHearingService onlineHearingService,
        QuestionService questionService
    ) {
        this.onlineHearingService = onlineHearingService;
        this.questionService = questionService;
    }

    @ApiOperation(value = "Request deadline extension",
        notes = "Issued questions will get better chance to be answered on time.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 404, message = "Not found"),
        @ApiResponse(code = 424, message = "Failed dependency"),
        @ApiResponse(code = 500, message = "General request failure")
    })
    @PutMapping("/deadline-extensions")
    public ResponseEntity requestExtensionForQuestion(@PathVariable UUID onlineHearingId) {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingId);
        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearing);

        if (!optionalOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Online hearing not found");
        }

        try {
            questionService.requestDeadlineExtension(optionalOnlineHearing.get());
        } catch (Exception e) {
            log.error("Request failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Request failed. See logs for details.");
        } catch (NoQuestionsAsked e) {
            log.warn("Deadline extension request for hearing without questions; hearingId={}", onlineHearingId);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body("No questions to extend deadline for.");
        }
        return ResponseEntity.ok(null);
    }
}
