package uk.gov.hmcts.reform.coh.controller.question;

import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponseMapper;
import uk.gov.hmcts.reform.coh.controller.utils.CohISO8601DateFormat;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.Question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public enum QuestionResponseMapper {

    QUESTION_ID((Question q) -> q.getQuestionId().toString(), QuestionResponse::setQuestionId),
    QUESTION_ROUND((Question q) -> q.getQuestionRound().toString(), QuestionResponse::setQuestionRound),
    QUESTION_ORDINAL((Question q) -> Integer.toString(q.getQuestionOrdinal()), QuestionRequest::setQuestionOrdinal),
    QUESTION_HEADER_TEXT(Question::getQuestionHeaderText, QuestionRequest::setQuestionHeaderText),
    QUESTION_BODY_TEXT(Question::getQuestionText, QuestionRequest::setQuestionBodyText),
    OWNER_REFERENCE(Question::getOwnerReferenceId, QuestionRequest::setOwnerReference),
    QUESTION_STATE(q -> q.getQuestionState().getState(), (qr, s) -> qr.getCurrentState().setName(s)),
    STATE_TIME(q -> {
        if (!q.getQuestionStateHistories().isEmpty()){
            return CohISO8601DateFormat.format(q.getQuestionStateHistories().get(q.getQuestionStateHistories().size()-1).getDateOccurred());
        }
        return null;
        }, (qr, s) -> qr.getCurrentState().setDatetime(s));

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
        if (question.getDeadlineExpiryDate() != null) {
            response.setDeadlineExpiryDate(question.getDeadlineExpiryDate());
        }

        List<Answer> answers = question.getAnswers();
        if (answers != null && !answers.isEmpty()) {
            List<AnswerResponse> answerResponses = new ArrayList<>();
            for (Answer answer : answers) {
                AnswerResponse answerResponse = new AnswerResponse();
                AnswerResponseMapper.map(answer, answerResponse);
                answerResponses.add(answerResponse);
            }
            response.setAnswers(answerResponses);
        }
    }

    public static void map(Question question, QuestionResponse response, Answer answer) {
        for (QuestionResponseMapper m : QuestionResponseMapper.class.getEnumConstants()) {
            m.set(question, response);
        }
        if(question.getDeadlineExpiryDate()!=null) {
            response.setDeadlineExpiryDate(question.getDeadlineExpiryDate());
        }
        AnswerResponse answerResponse = new AnswerResponse();
        AnswerResponseMapper.map(answer, answerResponse);
        response.setAnswers(Arrays.asList(answerResponse));
    }

    public void set(Question question, QuestionResponse questionResponse) {
        setter.accept(questionResponse, getter.apply(question));
    }

}
