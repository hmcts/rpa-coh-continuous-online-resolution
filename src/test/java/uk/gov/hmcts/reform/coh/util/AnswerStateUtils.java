package uk.gov.hmcts.reform.coh.util;

import uk.gov.hmcts.reform.coh.domain.AnswerState;
import uk.gov.hmcts.reform.coh.states.AnswerStates;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class AnswerStateUtils {

    private static Map<AnswerStates, AnswerState> mappings;

    static {
        mappings = new HashMap<>();
        Arrays.stream(AnswerStates.values()).forEach(as -> mappings.put(as, new AnswerState(mappings.size() + 1, as.getStateName())));
    }

    private AnswerStateUtils() {
    }

    public static final AnswerState get(AnswerStates state) {
        return mappings.get(state);
    }
}
