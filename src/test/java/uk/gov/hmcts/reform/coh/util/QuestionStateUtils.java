package uk.gov.hmcts.reform.coh.util;

import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class QuestionStateUtils {

    private static Map<QuestionStates, QuestionState> mappings;

    static {
        mappings = new HashMap<>();
        Arrays.stream(QuestionStates.values()).forEach(qs -> mappings.put(qs, new QuestionState(mappings.size() + 1, qs.getStateName())));
    }

    private QuestionStateUtils() {
    }

    public static final QuestionState get(QuestionStates questionStates) {
        return mappings.get(questionStates);
    }
}
