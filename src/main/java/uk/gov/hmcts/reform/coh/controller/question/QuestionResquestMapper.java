package uk.gov.hmcts.reform.coh.controller.question;

import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;

public class QuestionResquestMapper {

    private QuestionRequest questionRequest;

    private Question question;

    private OnlineHearing onlineHearing;

    public QuestionResquestMapper(Question question, OnlineHearing onlineHearing, QuestionRequest questionRequest) {
        this.questionRequest = questionRequest;
        this.question = question;
    }

    public void map() {
        question.onlineHearing(onlineHearing)
                .questionOrdinal(questionRequest.getQuestionOrdinal())
                .questionHeaderText(questionRequest.getQuestionHeaderText())
                .questionText(questionRequest.getQuestionBodyText())
                .questionRound(questionRequest.getQuestionRound())
                .ownerReferenceId(questionRequest.getOwnerReference());
    }
}
