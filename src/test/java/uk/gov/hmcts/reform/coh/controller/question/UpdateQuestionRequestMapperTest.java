package uk.gov.hmcts.reform.coh.controller.question;

import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.coh.domain.Question;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateQuestionRequestMapperTest {

    private UpdateQuestionRequest request;
    private Question question;

    @Before
    public void setUp() {
        request = new UpdateQuestionRequest();
        request.setQuestionRound("1");
        question = new Question();
    }

    @Test
    public void maps_question_text() {
        request.setQuestionBodyText("Requested");
        question.setQuestionText("Current");

        UpdateQuestionRequestMapper.map(question, request);

        assertThat(question.getQuestionText()).isEqualTo(request.getQuestionBodyText());
    }

    @Test
    public void maps_question_header() {
        request.setQuestionHeaderText("Requested");
        question.setQuestionHeaderText("Current");

        UpdateQuestionRequestMapper.map(question, request);

        assertThat(question.getQuestionHeaderText()).isEqualTo(request.getQuestionHeaderText());
    }

    @Test
    public void maps_question_round() {
        request.setQuestionRound("1");
        question.setQuestionRound(0);

        UpdateQuestionRequestMapper.map(question, request);

        assertThat(question.getQuestionRound()).isEqualTo(Integer.parseInt(request.getQuestionRound()));
    }

    @Test
    public void maps_linked_question_id() {
        request.setLinkedQuestionId(ImmutableSet.of(UUID.fromString("70e81346-df7b-420a-bd8e-419825d10a27")));
        question.setLinkedQuestions(ImmutableSet.of(UUID.fromString("deee64ad-7cd1-4904-ab93-3561f842f2ba")));

        UpdateQuestionRequestMapper.map(question, request);

        assertThat(question.getLinkedQuestions()).containsExactlyInAnyOrderElementsOf(request.getLinkedQuestionId());
    }

    @Test
    public void maps_owner_id() {
        request.setOwnerReference("The Owner");
        question.setOwnerReferenceId("To be updated");

        UpdateQuestionRequestMapper.map(question, request);

        assertThat(question.getOwnerReferenceId()).isEqualTo(request.getOwnerReference());
    }
}
