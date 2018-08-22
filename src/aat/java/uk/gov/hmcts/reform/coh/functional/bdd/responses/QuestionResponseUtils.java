package uk.gov.hmcts.reform.coh.functional.bdd.responses;

import uk.gov.hmcts.reform.coh.controller.question.CreateQuestionResponse;
import uk.gov.hmcts.reform.coh.domain.Question;

public class QuestionResponseUtils {

    public static final Question getQuestion(CreateQuestionResponse createQuestionResponse) {
        Question question = new Question();
        question.setQuestionId(createQuestionResponse.getQuestionId());

        return question;
    }
}
