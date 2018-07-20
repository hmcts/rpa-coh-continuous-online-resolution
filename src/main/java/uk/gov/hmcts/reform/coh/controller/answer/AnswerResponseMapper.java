package uk.gov.hmcts.reform.coh.controller.answer;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.AnswerStateHistory;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public enum AnswerResponseMapper {

    ANSWER_ID(a -> a.getAnswerId().toString(), AnswerResponse::setAnswerId),
    ANSWER_TEXT(Answer::getAnswerText, AnswerResponse::setAnswerText),
    ANSWER_STATE_NAME(a -> a.getAnswerState().getState(), (ar, s) -> ar.getStateResponse().setName(s)),
    ANSWER_STATE_DATETIME(a -> {
            if (a.getAnswerStateHistories() != null && !a.getAnswerStateHistories().isEmpty()) {
                ISO8601DateFormat df = new ISO8601DateFormat();
                Optional<AnswerStateHistory> history = a.getAnswerStateHistories().stream().sorted(Comparator.comparing(AnswerStateHistory::getDateOccured).reversed()).findFirst();
                return df.format(history.get().getDateOccured());
            }
            return null;
        }, (ar, s) -> ar.getStateResponse().setDatetime(s));

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
