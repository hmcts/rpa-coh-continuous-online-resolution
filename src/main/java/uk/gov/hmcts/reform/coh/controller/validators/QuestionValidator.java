package uk.gov.hmcts.reform.coh.controller.validators;

import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.coh.controller.question.QuestionRequest;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.coh.controller.validators.ValidatorUtils.isPositiveInteger;

public enum QuestionValidator implements Validator<QuestionRequest>{

    QUESTION_ROUND(qr -> StringUtils.isEmpty(qr.getQuestionRound())
            || !isPositiveInteger(qr.getQuestionRound()), "Question round is required and must be numeric"),
    QUESTION_ORDINAL(qr -> StringUtils.isEmpty(qr.getQuestionOrdinal())
            || !isPositiveInteger(qr.getQuestionOrdinal()), "Question ordinal is required and must be numeric"),
    QUESTION_HEADER(qr -> StringUtils.isEmpty(qr.getQuestionHeaderText()), "Question header text is required"),
    QUESTION_BODY(qr -> StringUtils.isEmpty(qr.getQuestionBodyText()), "Question body text is required"),
    QUESTION_OWNER(qr -> StringUtils.isEmpty(qr.getOwnerReference()), "Owner reference is required");

    private Predicate<QuestionRequest> predicate;
    private String message;

    QuestionValidator(Predicate<QuestionRequest> predicate, String message) {
        this.predicate = predicate;
        this.message = message;
    }

    public boolean test(QuestionRequest request) {
        return predicate.test(request);
    }

    public String getMessage() {
        return message;
    }
}
