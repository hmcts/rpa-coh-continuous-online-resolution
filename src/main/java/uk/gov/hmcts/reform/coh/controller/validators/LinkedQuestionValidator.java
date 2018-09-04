package uk.gov.hmcts.reform.coh.controller.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.controller.question.QuestionRequest;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class LinkedQuestionValidator implements BiValidator<OnlineHearing, QuestionRequest> {

    @Autowired
    private QuestionRepository questionRepository;

    private Validation validation = new Validation();

    private String message;

    @Override
    public boolean test(OnlineHearing onlineHearing, QuestionRequest questionRequest) {

        List<UUID> questions = questionRequest.getLinkedQuestionId()
                .stream()
                .filter(id -> {
                    Optional<Question> q = questionRepository.findById(id);
                    return !q.isPresent() || !q.get().getOnlineHearing().equals(onlineHearing);})
                .collect(Collectors.toList());

        if (!questions.isEmpty()) {
            this.message = String.format("Linked question '%s' must belong to the same online hearing", questions.get(0));
            return true;
        }

        ValidationResult result = validation.execute(QuestionValidator.values(), questionRequest);
        if (!result.isValid()) {
            this.message = result.getReason();
            return result.isValid();
        }

        return false;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
