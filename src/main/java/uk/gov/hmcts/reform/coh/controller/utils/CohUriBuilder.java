package uk.gov.hmcts.reform.coh.controller.utils;

import java.util.UUID;

public class CohUriBuilder {

    public static String buildOnlineHearingPost() {

        return "/continuous-online-hearings";
    }

    public static String buildOnlineHearingGet(UUID onlineHearingId) {

        return buildOnlineHearingPost() + "/" + onlineHearingId;
    }

    public static String buildQuestionPost(UUID onlineHearingId) {

        return buildOnlineHearingGet(onlineHearingId) + "/questions";
    }

    public static String buildQuestionGet(UUID onlineHearingId, UUID questionId) {

        return buildQuestionPost(onlineHearingId) + "/" + questionId;
    }

    public static String buildDecisionGet(UUID onlineHearingId) {

        return buildOnlineHearingGet(onlineHearingId) + "/decisions";
    }

    public static String buildAnswerPost(UUID onlineHearingId, UUID questionId) {

        return buildQuestionGet(onlineHearingId, questionId) + "/answers";
    }

    public static String buildAnswerGet(UUID onlineHearingId, UUID questionId, UUID answerId) {

        return buildAnswerPost(onlineHearingId, questionId) + "/" + answerId;
    }

    public static String buildDecisionReplyPost(UUID onlineHearingId) {
        return buildOnlineHearingGet(onlineHearingId) + "/decisionreplies";
    }

    public static String buildDecisionReplyGet(UUID onlineHearingId, UUID decisionReplyId) {

        return buildDecisionReplyPost(onlineHearingId) + "/" + decisionReplyId;
    }
}
