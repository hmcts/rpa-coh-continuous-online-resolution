package uk.gov.hmcts.reform.coh.controller.conversations;

import uk.gov.hmcts.reform.coh.controller.question.QuestionResponse;
import uk.gov.hmcts.reform.coh.controller.question.QuestionResponseMapper;
import uk.gov.hmcts.reform.coh.controller.state.StateResponse;
import uk.gov.hmcts.reform.coh.controller.utils.CohISO8601DateFormat;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.domain.Question;

import java.util.stream.Collectors;

public class QuestionConversationMapper {

    private QuestionConversationMapper() {}

    public static void map(Question question, QuestionResponse response) {
        QuestionResponseMapper.map(question, response);

        if (question.getQuestionStateHistories() != null && !question.getQuestionStateHistories().isEmpty()) {
            response.setHistories(
                    question.getQuestionStateHistories()
                            .stream()
                            .map(h -> new StateResponse(h.getQuestionstate().getState(), CohISO8601DateFormat.format(h.getDateOccurred())))
                            .collect(Collectors.toList()
                            )
            );
        }

        response.setUri(CohUriBuilder.buildQuestionGet(question.getOnlineHearing().getOnlineHearingId(), question.getQuestionId()));
    }
}
