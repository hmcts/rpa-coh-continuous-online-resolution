package uk.gov.hmcts.reform.coh.controller.utils;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class CohUriBuilderTest {

    private UUID onlineHearingId = UUID.randomUUID();
    private UUID questionId = UUID.randomUUID();
    private UUID answerId = UUID.randomUUID();
    private UUID decisionReplyId = UUID.randomUUID();
    private String baseUri;
    private String onlineHearingUri;
    private String decisionUri;
    private String decisionRepliesUri;
    private String decisionRepliesGetUri;
    private String questionUri;
    private String questionGetUri;
    private String questionRoundGetAllUri;
    private String questionRoundGetUri;
    private String answerUri;
    private String answerGetUri;
    private String conversationsUri;

    @Before
    public void setUp() {
        baseUri = "/continuous-online-hearings";
        onlineHearingUri = baseUri + "/" + onlineHearingId;
        decisionUri = onlineHearingUri + "/decisions";
        questionUri = onlineHearingUri + "/questions";
        questionGetUri = questionUri + "/" + questionId;
        questionUri = onlineHearingUri + "/questions";
        questionRoundGetUri = onlineHearingUri + "/questionrounds/1";
        questionRoundGetAllUri = onlineHearingUri + "/questionrounds/";
        answerUri = questionGetUri + "/answers";
        answerGetUri = answerUri + "/" + answerId;
        decisionRepliesUri = onlineHearingUri + "/decisionreplies";
        decisionRepliesGetUri = onlineHearingUri + "/decisionreplies/" + decisionReplyId;
        conversationsUri = onlineHearingUri + "/conversations";
    }

    @Test
    public void testOnlineHearingPost() {
        assertEquals(baseUri, CohUriBuilder.buildOnlineHearingPost());
    }

    @Test
    public void testOnlineHearingGet() {
        assertEquals(onlineHearingUri, CohUriBuilder.buildOnlineHearingGet(onlineHearingId));
    }

    @Test
    public void testQuestionPost() {
        assertEquals(questionUri, CohUriBuilder.buildQuestionPost(onlineHearingId));
    }

    @Test
    public void testQuestionGet() {
        assertEquals(questionGetUri, CohUriBuilder.buildQuestionGet(onlineHearingId, questionId));
    }

    @Test
    public void testQuestionRoundGet() {
        assertEquals(questionRoundGetUri, CohUriBuilder.buildQuestionRoundGet(onlineHearingId, 1));
    }

    @Test
    public void testQuestionRoundGetAll() {
        assertEquals(questionRoundGetAllUri, CohUriBuilder.buildQuestionRoundGetAll(onlineHearingId));
    }

    @Test
    public void testDecisionGet() {
        assertEquals(decisionUri, CohUriBuilder.buildDecisionGet(onlineHearingId));
    }

    @Test
    public void testAnswersPost() {
        assertEquals(answerUri, CohUriBuilder.buildAnswerPost(onlineHearingId, questionId));
    }

    @Test
    public void testAnswersGet() {
        assertEquals(answerGetUri, CohUriBuilder.buildAnswerGet(onlineHearingId, questionId, answerId));
    }

    @Test
    public void testDecisionRepliesPost() {
        assertEquals(decisionRepliesUri, CohUriBuilder.buildDecisionReplyPost(onlineHearingId));
    }

    @Test
    public void testDecisionRepliesGet() {
        assertEquals(decisionRepliesGetUri, CohUriBuilder.buildDecisionReplyGet(onlineHearingId, decisionReplyId));
    }

    @Test
    public void testConversationsGet() {
        assertEquals(conversationsUri, CohUriBuilder.buildConversationsGet(onlineHearingId));
    }
}