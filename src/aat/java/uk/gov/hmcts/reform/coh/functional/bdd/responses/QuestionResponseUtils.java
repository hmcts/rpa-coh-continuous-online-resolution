package uk.gov.hmcts.reform.coh.functional.bdd.responses;

import uk.gov.hmcts.reform.coh.controller.question.CreateQuestionResponse;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;

import static uk.gov.hmcts.reform.coh.utils.JsonUtils.toObjectFromJson;

public class QuestionResponseUtils {

    public static final Question getQuestion(CreateQuestionResponse createQuestionResponse) {
        Question question = new Question();
        question.setQuestionId(createQuestionResponse.getQuestionId());

        return question;
    }

    public static final CreateQuestionResponse getCreateQuestionResponse(String payload) throws Exception {
        return toObjectFromJson(payload, CreateQuestionResponse.class);
    }
}
