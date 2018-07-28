package uk.gov.hmcts.reform.coh.controller.decision;

import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionStateHistory;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum DecisionResponseMapper {

    DECISION_ID(d -> d.getDecisionId().toString(), DecisionResponse::setDecisionId),
    ONLINE_HEARING_ID(d -> d.getOnlineHearing().getOnlineHearingId().toString(), DecisionResponse::setOnlineHearingId),
    DECISION_HEADER(Decision::getDecisionHeader, DecisionResponse::setDecisionHeader),
    DECISION_TEXT(Decision::getDecisionText, DecisionResponse::setDecisionText),
    DECISION_REASON(Decision::getDecisionReason, DecisionResponse::setDecisionReason),
    DECISION_AWARD(Decision::getDecisionAward, DecisionResponse::setDecisionAward),
    DEADLINE_EXPIRY_DATE(d -> {
        if (d.getDeadlineExpiryDate() != null) {
            return d.getDeadlineExpiryDate().toString();
        }
        return null;
    }, DecisionResponse::setDeadlineExpiryDate),
    DECISION_STATE_NAME(d -> d.getDecisionstate().getState(), DecisionResponse::setDecisionStateName),
    DECISION_STATE_TIMESTAMP(
            d ->
            {
                String date = null;
                if (d.getDecisionStateHistories() != null && !d.getDecisionStateHistories().isEmpty()) {
                    date = d.getDecisionStateHistories()
                            .stream()
                            .sorted(Comparator.comparing(DecisionStateHistory::getDateOccured).reversed())
                            .findFirst().get().getDateOccured()
                            .toString();
                }

                return date;
            }
            , DecisionResponse::setDecisionStateDatetime
    );

    private Function<Decision, String> decision;

    private Optional<Function<DecisionStateHistory, String>> decisionStateHistory;

    private BiConsumer<DecisionResponse, String> setter;

    DecisionResponseMapper(Function<Decision, String> decision, BiConsumer<DecisionResponse, String> setter) {
        this.decision = decision;
        this.setter = setter;
    }

    public static void map(Decision decision, DecisionResponse response) {
        for (DecisionResponseMapper m : DecisionResponseMapper.class.getEnumConstants()) {
            m.set(decision, response);
        }
    }

    public void set(Decision decision, DecisionResponse response) {
        setter.accept(response, this.decision.apply(decision));
    }
}

