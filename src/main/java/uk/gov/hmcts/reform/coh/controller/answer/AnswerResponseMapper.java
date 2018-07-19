package uk.gov.hmcts.reform.coh.controller.answer;

import uk.gov.hmcts.reform.coh.domain.Answer;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum AnswerResponseMapper {

    ANSWER_ID(a -> a.getAnswerId().toString(), AnswerResponse::setAnswerId),
    ANSWER_TEXT(Answer::getAnswerText, AnswerResponse::setAnswerText),
    ANSWER_STATE_NAME(a -> a.getAnswerState().getState(), (ar, s) -> ar.getStateResponse().setName(s)),
    ANSWER_STATE_DATETIME();

    private Function<Answer, String> getter;

    private BiConsumer<AnswerResponse, String> setter;

    AnswerResponseMapper(Function<Answer, String> getter, BiConsumer<AnswerResponse, String> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public static void map(Answer answer, AnswerResponse response) {
        for (AnswerResponseMapper m : AnswerResponseMapper.class.getEnumConstants()) {
            m.set(answer, response);
        }
    }

    public void set(Answer answer, AnswerResponse response) {
        setter.accept(response, getter.apply(answer));
    }
}
