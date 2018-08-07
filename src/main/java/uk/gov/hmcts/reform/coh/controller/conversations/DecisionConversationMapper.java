package uk.gov.hmcts.reform.coh.controller.conversations;

import uk.gov.hmcts.reform.coh.controller.decision.DecisionResponse;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionResponseMapper;
import uk.gov.hmcts.reform.coh.controller.state.StateResponse;
import uk.gov.hmcts.reform.coh.controller.utils.CohISO8601DateFormat;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionStateHistory;

import java.util.Comparator;
import java.util.stream.Collectors;

public class DecisionConversationMapper {

    public static void map(Decision decision, DecisionResponse response) {

        DecisionResponseMapper.map(decision, response);

        if (decision.getDecisionStateHistories() != null && !decision.getDecisionStateHistories().isEmpty()) {
            response.setHistories(
                    decision.getDecisionStateHistories()
                            .stream()
                            .sorted(Comparator.comparing(DecisionStateHistory::getDateOccured))
                            .map(h -> new StateResponse(h.getDecisionstate().getState(), CohISO8601DateFormat.format(h.getDateOccured())))
                            .collect(Collectors.toList()
                            )
            );
        }

        response.setUri(CohUriBuilder.buildDecisionGet(decision.getOnlineHearing().getOnlineHearingId()));
    }
}
