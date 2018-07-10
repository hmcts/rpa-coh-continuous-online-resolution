package uk.gov.hmcts.reform.coh.controller.question;

import uk.gov.hmcts.reform.coh.domain.Onlinehearing;
import uk.gov.hmcts.reform.coh.domain.Question;

public class QuestionResquestMapper {

    private QuestionRequest questionRequest;

    private Question question;

    private Onlinehearing onlinehearing;

    public QuestionResquestMapper(Question question, Onlinehearing onlinehearing, QuestionRequest questionRequest) {
        this.questionRequest = questionRequest;
        this.question = question;
    }

    public void map() {
        question.onlinehearing(onlinehearing)
                .questionOrdinal(Integer.parseInt(questionRequest.getQuestionOrdinal()))
                .questionHeaderText(questionRequest.getQuestionHeaderText())
                .questionText(questionRequest.getQuestionBodyText())
                .questionRound(Integer.parseInt(questionRequest.getQuestionRound()))
                .ownerReferenceId(questionRequest.getOwnerReference());
    }
}
