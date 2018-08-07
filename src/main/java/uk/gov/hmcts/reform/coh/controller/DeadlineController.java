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
import uk.gov.hmcts.reform.coh.controller.state.DeadlineExtensionHelper;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.service.SessionEventService;
import uk.gov.hmcts.reform.coh.service.exceptions.NoQuestionsAsked;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/continuous-online-hearings/{onlineHearingId}")
public class DeadlineController {

    private static final Logger log = LoggerFactory.getLogger(DeadlineController.class);

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private SessionEventService sessionEventService;

    public DeadlineController() {
    }

    @ApiOperation(value = "Request deadline extension",
        notes = "A PUT request to extend the deadline of questions.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 404, message = "Not found"),
        @ApiResponse(code = 424, message = "Failed dependency"),
        @ApiResponse(code = 500, message = "General request failure")
    })
    @PutMapping("/questions-deadline-extension")
    public ResponseEntity requestExtensionForQuestion(@PathVariable UUID onlineHearingId) {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingId);
        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearing);

        if (!optionalOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Online hearing not found");
        }

        try {
            DeadlineExtensionHelper helper  = questionService.requestDeadlineExtension(optionalOnlineHearing.get());
            queueSessionEvent(optionalOnlineHearing.get(), helper);
        } catch (Exception e) {
            log.error("Request failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Request failed. " + e.getMessage());
        } catch (NoQuestionsAsked e) {
            log.warn("Deadline extension request for hearing without questions; hearingId={}", onlineHearingId);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body("No questions to extend deadline for.");
        }
        return ResponseEntity.ok().build();
    }

    private void queueSessionEvent(OnlineHearing onlineHearing, DeadlineExtensionHelper helper) {

        if ((helper.getEligible() <= 0)
            || (helper.getGranted() <=0 && helper.getDenied() <= 0)) {

            // Nothing was eligible for extension or (nothing was granted or denied)
            return;
        }

        EventTypes eventTypes = helper.getGranted() > 0 ? EventTypes.QUESTION_DEADLINE_EXTENSION_GRANTED : EventTypes.QUESTION_DEADLINE_EXTENSION_DENIED;
        sessionEventService.createSessionEvent(onlineHearing, eventTypes.getEventType());
    }
}
