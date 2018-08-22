package uk.gov.hmcts.reform.coh.controller.question;

import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;

public class QuestionRequestMapper {

    private QuestionRequest questionRequest;

    private Question question;

    private OnlineHearing onlineHearing;

    public QuestionRequestMapper(Question question, OnlineHearing onlineHearing, QuestionRequest questionRequest) {
        this.questionRequest = questionRequest;
        this.question = question;
        this.onlineHearing = onlineHearing;
    }

    public void map() {
        question.onlineHearing(onlineHearing)
                .questionOrdinal(Integer.parseInt(questionRequest.getQuestionOrdinal()))
                .questionHeaderText(questionRequest.getQuestionHeaderText())
                .questionText(questionRequest.getQuestionBodyText())
                .questionRound(Integer.parseInt(questionRequest.getQuestionRound()))
                .ownerReferenceId(questionRequest.getOwnerReference());
    }
}
