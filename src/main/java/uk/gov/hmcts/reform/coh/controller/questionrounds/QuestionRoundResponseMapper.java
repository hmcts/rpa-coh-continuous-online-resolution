package uk.gov.hmcts.reform.coh.controller.questionrounds;

import uk.gov.hmcts.reform.coh.controller.question.QuestionResponse;
import uk.gov.hmcts.reform.coh.controller.question.QuestionResponseMapper;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public enum QuestionRoundResponseMapper {
    QUESTION_ROUND((QuestionRound q) -> { return q.getQuestionRound().toString(); }, QuestionRoundResponse::setQuestionRound);
//    QUESTION_ROUND_STATE(q -> {return q.getQuestionRoundState().getState();}, (qr, s) -> { qr.getQuestionRoundState().setState(s);});

    private Function<QuestionRound, String> getter;
    private BiConsumer<QuestionRoundResponse, String> setter;

    QuestionRoundResponseMapper (Function<QuestionRound, String> getter, BiConsumer<QuestionRoundResponse, String> setter){
        this.getter = getter;
        this.setter = setter;
    }

    public static void map(QuestionRound questionRound, QuestionRoundResponse questionRoundResponse) {
        for (QuestionRoundResponseMapper m : QuestionRoundResponseMapper.class.getEnumConstants()) {
            m.set(questionRound, questionRoundResponse);
        }

        List<Question> questions = questionRound.getQuestionList();
        for(Question question : questions) {
            QuestionResponse questionResponse = new QuestionResponse();
            QuestionResponseMapper.map(question, questionResponse);
            questionRoundResponse.addQuestionResponse(questionResponse);
        }
    }

    public void set(QuestionRound questionRound, QuestionRoundResponse questionRoundResponse) {
        setter.accept(questionRoundResponse, getter.apply(questionRound));
    }
}
