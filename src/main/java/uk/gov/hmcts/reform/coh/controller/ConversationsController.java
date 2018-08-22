package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.controller.conversations.AnswerConversationMapper;
import uk.gov.hmcts.reform.coh.controller.conversations.DecisionConversationMapper;
import uk.gov.hmcts.reform.coh.controller.conversations.DecisionReplyConversationMapper;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionResponse;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.DecisionReplyResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.ConversationResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingMapper;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingResponse;
import uk.gov.hmcts.reform.coh.controller.conversations.QuestionConversationMapper;
import uk.gov.hmcts.reform.coh.controller.question.QuestionResponse;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionReply;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.DecisionReplyService;
import uk.gov.hmcts.reform.coh.service.DecisionService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.coh.controller.utils.CommonMessages.ONLINE_HEARING_NOT_FOUND;

@RestController
@RequestMapping("/continuous-online-hearings/{onlineHearingId}/conversations")
public class ConversationsController {

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private DecisionService decisionService;

    @Autowired
    private DecisionReplyService decisionReplyService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;


    @ApiOperation(value = "Get a conversation", notes = "A GET request to retrieve an online hearing conversation")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response =  ConversationResponse.class),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @GetMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getConversations(@PathVariable UUID onlineHearingId) {

        Optional<OnlineHearing> optOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearingId);
        if (!optOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ONLINE_HEARING_NOT_FOUND);
        }

        ConversationResponse response = new ConversationResponse();
        OnlineHearing onlineHearing = optOnlineHearing.get();
        mapOnlineHearing(onlineHearing, response);

        return ResponseEntity.ok(response);
    }

    public void mapOnlineHearing(OnlineHearing onlineHearing, ConversationResponse response) {

        OnlineHearingResponse onlineHearingResponse = new OnlineHearingResponse();
        response.setOnlineHearing(onlineHearingResponse);
        OnlineHearingMapper.map(onlineHearingResponse, onlineHearing);
        onlineHearingResponse.setUri(CohUriBuilder.buildOnlineHearingGet(onlineHearing.getOnlineHearingId()));

        mapDecision(onlineHearing, onlineHearingResponse);
        mapQuestions(onlineHearing, onlineHearingResponse);
   }

    public void mapDecision(OnlineHearing onlineHearing, OnlineHearingResponse response) {
        Optional<Decision> optDecision = decisionService.findByOnlineHearingId(onlineHearing.getOnlineHearingId());
        if(optDecision.isPresent()) {
            DecisionResponse decisionResponse = new DecisionResponse();
            DecisionConversationMapper.map(optDecision.get(), decisionResponse);
            mapDecisionReplies(optDecision.get(), decisionResponse);
            response.setDecisionResponse(decisionResponse);
        }
    }

    private void mapDecisionReplies(Decision decision, DecisionResponse response) {
        List<DecisionReply> decisionReplies = decisionReplyService.findAllDecisionReplyByDecision(decision);
        response.setDecisionReplies(
            decisionReplies.stream()
                        .map(dr -> {
                            DecisionReplyResponse drr = new DecisionReplyResponse();
                            DecisionReplyConversationMapper.map(dr, drr);
                            return drr;
                        })
                        .collect(Collectors.toList())
        );
    }

    public void mapQuestions(OnlineHearing onlineHearing, OnlineHearingResponse response) {
        Optional<List<Question>> optionalQuestions = questionService.findAllQuestionsByOnlineHearing(onlineHearing);
        if(optionalQuestions.isPresent()) {
            List<QuestionResponse> questionResponses =
                    optionalQuestions
                            .get()
                            .stream()
                            .map( q -> {
                                QuestionResponse qr = new QuestionResponse();
                                QuestionConversationMapper.map(q, qr);
                                mapAnswers(q, qr);
                                return qr;
                            }
                            )
                            .collect(Collectors.toList());

            response.setQuestions(questionResponses);
        }
    }

    public void mapAnswers(Question question, QuestionResponse response) {
        List<Answer> answers = answerService.retrieveAnswersByQuestion(question);
        response.setAnswers(
                answers.stream()
                        .map(a -> {
                            AnswerResponse ar = new AnswerResponse();
                            AnswerConversationMapper.map(a, ar);
                            return ar;
                        }
                        )
                        .collect(Collectors.toList())
        );
    }
}
