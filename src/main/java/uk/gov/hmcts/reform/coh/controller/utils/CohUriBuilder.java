package uk.gov.hmcts.reform.coh.controller.utils;

import java.util.UUID;

public class CohUriBuilder {

    public static String buildOnlineHearingGet(UUID onlineHearingId) {

        return String.format("/continuous-online-hearings/%s", onlineHearingId);
    }

    public static String buildQuestionGet(UUID onlineHearingId, UUID questionId) {

        return String.format("/continuous-online-hearings/%s/questions/%s", onlineHearingId, questionId);
    }
}
