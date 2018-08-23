package uk.gov.hmcts.reform.coh.functional.bdd.responses;

import uk.gov.hmcts.reform.coh.controller.answer.CreateAnswerResponse;
import uk.gov.hmcts.reform.coh.domain.Answer;

final public class AnswerResponseUtils {

    private AnswerResponseUtils(){}

    public static final Answer getAnswer(CreateAnswerResponse createAnswerResponse) {
        Answer answer = new Answer();
        answer.setAnswerId(createAnswerResponse.getAnswerId());

        return answer;
    }
}
