package uk.gov.hmcts.reform.coh.controller.question;

import uk.gov.hmcts.reform.coh.domain.Question;

import java.util.Date;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public enum QuestionResponseMapper {

    QUESTION_ID((Question q) -> { return q.getQuestionId().toString(); }, QuestionResponse::setQuestionId),
    QUESTION_ROUND((Question q) -> {return q.getQuestionRound().toString(); }, QuestionResponse::setQuestionRound),
    QUESTION_ORDINAL((Question q) -> {return q.getQuestionRound().toString(); }, QuestionRequest::setQuestionOrdinal),
    QUESTION_HEADER_TEXT(Question::getQuestionHeaderText, QuestionRequest::setQuestionHeaderText),
    QUESTION_BODY_TEXT(Question::getQuestionText, QuestionRequest::setQuestionBodyText),
    OWNER_REFERENCE(Question::getOwnerReferenceId, QuestionRequest::setOwnerReference),
    QUESTION_STATE(q -> {return q.getQuestionState().getState();}, (qr, s) -> { qr.setCurrentState("state_name", s);}),
    STATE_TIME(q -> {return q.getCurrentQuestionState().getDateOccurred().toString();}, (qr, s) -> { qr.setCurrentState("state_datetime", s);});

    private Function<Question, String> getter;
    private BiConsumer<QuestionResponse, String> setter;

    QuestionResponseMapper (Function<Question, String> getter, BiConsumer<QuestionResponse, String> setter){
        this.getter = getter;
        this.setter = setter;
    }

    public static void map(Question question, QuestionResponse response) {
        for (QuestionResponseMapper m : QuestionResponseMapper.class.getEnumConstants()) {
            m.set(question, response);
        }
    }

    public void set(Question question, QuestionResponse questionResponse) {
        setter.accept(questionResponse, getter.apply(question));
    }
}
