package uk.gov.hmcts.reform.coh.controller.timelines;

import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponseMapper;
import uk.gov.hmcts.reform.coh.controller.state.StateResponse;
import uk.gov.hmcts.reform.coh.controller.utils.CohISO8601DateFormat;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.AnswerStateHistory;
import uk.gov.hmcts.reform.coh.domain.Question;

import java.util.Comparator;
import java.util.stream.Collectors;

public class AnswerTimelinesMapper {

    public static void map(Answer answer, AnswerResponse response) {
        AnswerResponseMapper.map(answer, response);

        if (answer.getAnswerStateHistories() != null && !answer.getAnswerStateHistories().isEmpty()) {
            response.setHistories(
                    answer.getAnswerStateHistories()
                            .stream()
                            .sorted(Comparator.comparing(AnswerStateHistory::getDateOccured))
                            .map(h ->
                                    new StateResponse(h.getAnswerstate().getState(), CohISO8601DateFormat.format(h.getDateOccured()))
                            )
                            .collect(Collectors.toList())
            );
        }
        Question question =  answer.getQuestion();
        response.setUri(CohUriBuilder.buildAnswerGet(question.getOnlineHearing().getOnlineHearingId(), question.getQuestionId(), answer.getAnswerId()));
    }
}
