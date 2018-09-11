package uk.gov.hmcts.reform.coh.util;

import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import java.util.UUID;

public class QuestionEntityUtils {

    public static final UUID STANDARD_UUID = UUID.fromString("a5c21792-ba9e-462c-be8d-eab83d4e07ad");

    public static final Question createTestQuestion() {
        return createTestQuestion(QuestionStates.DRAFTED);
    }

    public static final Question createTestQuestion(QuestionStates state) {
        return createTestQuestion(state, null);
    }

    public static final Question createTestQuestion(OnlineHearing onlineHearing) {
        return createTestQuestion(QuestionStates.DRAFTED, onlineHearing);
    }

    public static final Question createTestQuestion(QuestionStates state, OnlineHearing onlineHearing) {
        Question question = new Question();
        question.setQuestionId(STANDARD_UUID);
        question.setQuestionRound(1);
        question.setQuestionOrdinal(1);
        question.setQuestionHeaderText("question header");
        question.setQuestionText("question text");
        question.setOwnerReferenceId("bar");
        question.setQuestionState(QuestionStateUtils.get(state));
        question.setDeadlineExtCount(1);
        question.setOnlineHearing(onlineHearing);

        return question;
    }
}
