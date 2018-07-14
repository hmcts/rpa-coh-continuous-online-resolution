package uk.gov.hmcts.reform.coh.controller.validators;

import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.coh.controller.question.QuestionRequest;
import java.util.function.Predicate;

public enum QuestionValidator implements Validator<QuestionRequest>{

    QUESTION_ROUND(qr -> StringUtils.isEmpty(qr.getQuestionRound()), "Question round is required"),
    QUESTION_ORDINAL(qr -> StringUtils.isEmpty(qr.getQuestionOrdinal()), "Question ordinal is required"),
    QUESTION_HEADER(qr -> StringUtils.isEmpty(qr.getQuestionHeaderText()), "Question header text is required"),
    QUESTION_BODY(qr -> StringUtils.isEmpty(qr.getQuestionBodyText()), "Question body text is required"),
    QUESTION_OWNER(qr -> StringUtils.isEmpty(qr.getOwnerReference()), "Owner reference is required");

    private Predicate<QuestionRequest> predicate;
    private String message;

    QuestionValidator(Predicate<QuestionRequest> predicate, String message) {
        this.predicate = predicate;
        this.message = message;
    }

    public Predicate<QuestionRequest> getPredicate() {
        return predicate;
    }

    public String getMessage() {
        return message;
    }
}
