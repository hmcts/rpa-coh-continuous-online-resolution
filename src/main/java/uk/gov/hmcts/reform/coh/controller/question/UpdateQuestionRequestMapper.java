package uk.gov.hmcts.reform.coh.controller.question;

import uk.gov.hmcts.reform.coh.domain.Question;

public class UpdateQuestionRequestMapper {

    private UpdateQuestionRequestMapper() {}

    public static void map(Question question, UpdateQuestionRequest updateQuestionRequest) {
        question.setQuestionText(updateQuestionRequest.getQuestionBodyText());
        question.setQuestionHeaderText(updateQuestionRequest.getQuestionHeaderText());
        question.setQuestionOrdinal(Integer.parseInt(updateQuestionRequest.getQuestionRound()));
    }
}
