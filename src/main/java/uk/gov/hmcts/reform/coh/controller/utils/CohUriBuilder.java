package uk.gov.hmcts.reform.coh.controller.utils;

import java.util.UUID;

public class CohUriBuilder {

    public static String buildOnlineHearingGet(UUID onlineHearingId) {

        return String.format("/continuous-online-hearings/%s", onlineHearingId);
    }

    public static String buildQuestionGet(UUID onlineHearingId, UUID questionId) {

        return String.format("/continuous-online-hearings/%s/questions/%s", onlineHearingId, questionId);
    }

    public static String buildDecisionGet(UUID onlineHearingId) {

        return String.format("/continuous-online-hearings/%s/decisions", onlineHearingId);
    }

    public static String buildAnswerGet(UUID onlineHearingId, UUID questionId, UUID answerId) {

        return String.format("/continuous-online-hearings/%s/questions/%s/answers/%s", onlineHearingId, questionId, answerId);
    }
}
