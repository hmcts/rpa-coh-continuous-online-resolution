package uk.gov.hmcts.reform.coh.controller.question;

import org.junit.Test;
import uk.gov.hmcts.reform.coh.domain.Question;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateQuestionRequestMapperTest {

    @Test
    public void maps_owner_id() {
        UpdateQuestionRequest request = new UpdateQuestionRequest();
        request.setQuestionRound("1");
        request.setOwnerReference("The Owner");

        Question question = new Question();
        question.setOwnerReferenceId("To be updated");

        UpdateQuestionRequestMapper.map(question, request);

        assertThat(question.getOwnerReferenceId()).isEqualTo(request.getOwnerReference());
    }
}
