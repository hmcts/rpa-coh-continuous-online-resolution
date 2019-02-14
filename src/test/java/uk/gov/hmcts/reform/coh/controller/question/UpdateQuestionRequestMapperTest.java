package uk.gov.hmcts.reform.coh.controller.question;

import com.google.common.collect.ImmutableSet;

import org.junit.Test;
import uk.gov.hmcts.reform.coh.domain.Question;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateQuestionRequestMapperTest {

    @Test
    public void maps_question_text() {
        UpdateQuestionRequest request = new UpdateQuestionRequest();
        request.setQuestionRound("1");
        request.setQuestionBodyText("Requested");

        Question question = new Question();
        question.setQuestionText("Current");

        UpdateQuestionRequestMapper.map(question, request);

        assertThat(question.getQuestionText()).isEqualTo(request.getQuestionBodyText());
    }

    @Test
    public void maps_question_header() {
        UpdateQuestionRequest request = new UpdateQuestionRequest();
        request.setQuestionRound("1");
        request.setQuestionHeaderText("Requested");

        Question question = new Question();
        question.setQuestionHeaderText("Current");

        UpdateQuestionRequestMapper.map(question, request);

        assertThat(question.getQuestionHeaderText()).isEqualTo(request.getQuestionHeaderText());
    }

    @Test
    public void maps_question_round() {
        UpdateQuestionRequest request = new UpdateQuestionRequest();
        request.setQuestionRound("1");

        Question question = new Question();
        question.setQuestionOrdinal(0);

        UpdateQuestionRequestMapper.map(question, request);

        assertThat(question.getQuestionOrdinal()).isEqualTo(Integer.parseInt(request.getQuestionRound()));
    }

    @Test
    public void maps_linked_question_id() {
        UpdateQuestionRequest request = new UpdateQuestionRequest();
        request.setQuestionRound("1");
        request.setLinkedQuestionId(ImmutableSet.of(UUID.randomUUID()));

        Question question = new Question();
        question.setLinkedQuestions(null);

        UpdateQuestionRequestMapper.map(question, request);

        assertThat(question.getLinkedQuestions()).containsExactlyInAnyOrderElementsOf(request.getLinkedQuestionId());
    }

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
