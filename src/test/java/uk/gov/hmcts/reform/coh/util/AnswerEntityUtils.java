package uk.gov.hmcts.reform.coh.util;

import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.AnswerState;
import uk.gov.hmcts.reform.coh.domain.Question;

import java.util.UUID;

public final class AnswerEntityUtils {

    public static final Answer createAnswer(Question question, AnswerState state) {
        Answer answer = new Answer();
        answer.setAnswerId(UUID.randomUUID());
        answer.setAnswerText("test answer");
        answer.setQuestion(question);
        answer.setAnswerState(state);

        return answer;
    }
}
