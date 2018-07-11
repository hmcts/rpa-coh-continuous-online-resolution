package uk.gov.hmcts.reform.coh.controller.question;

import uk.gov.hmcts.reform.coh.domain.Question;

public class UpdateQuestionRequestMapper {

    public static void map(Question question, UpdateQuestionRequest updateQuestionRequest) {
        question.setQuestionText(updateQuestionRequest.getQuestionText());
        question.setQuestionHeaderText(updateQuestionRequest.getQuestionHeaderText());
    }
}
