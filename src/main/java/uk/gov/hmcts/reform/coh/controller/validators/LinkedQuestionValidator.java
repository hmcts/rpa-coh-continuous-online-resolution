package uk.gov.hmcts.reform.coh.controller.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.controller.question.QuestionRequest;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LinkedQuestionValidator implements BiValidator<OnlineHearing, QuestionRequest> {

    @Autowired
    private QuestionRepository questionRepository;

    private Validation validation = new Validation();

    private String message;

    @Override
    public boolean test(OnlineHearing onlineHearing, QuestionRequest questionRequest) {

        List<UUID> questions = Optional.ofNullable(questionRequest.getLinkedQuestionId())
                .orElse(Collections.emptySet())
                .stream()
                .filter(id -> {
                    Optional<Question> q = questionRepository.findById(id);
                    return !q.isPresent() || isLinkedQuestionPartOfSameOnlineHearing(q.get(), onlineHearing);})
                .collect(Collectors.toList());

        if (!questions.isEmpty()) {
            this.message = String.format("Linked question '%s' must belong to the same online hearing", questions.get(0));
            return false;
        }

        ValidationResult result = validation.execute(QuestionValidator.values(), questionRequest);
        if (!result.isValid()) {
            this.message = result.getReason();
            return false;
        }

        return true;
    }

    @Override
    public String getMessage() {
        return message;
    }

    private boolean isLinkedQuestionPartOfSameOnlineHearing(Question q, OnlineHearing o) {
        return (q.getOnlineHearing() == null) || q.getOnlineHearing().equals(o);
    }
}
