package uk.gov.hmcts.reform.coh.util;

import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.states.OnlineHearingStates;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OnlineHearingStateUtils {

    private static Map<OnlineHearingStates, OnlineHearingState> mappings;

    static {
        mappings = new HashMap<>();
        Arrays.stream(OnlineHearingStates.values()).forEach(qs -> mappings.put(qs, new OnlineHearingState(mappings.size()+1, qs.getStateName())));
    }

    private OnlineHearingStateUtils() {
    }

    public static final OnlineHearingState get(OnlineHearingStates state) {
        return mappings.get(state);
    }
}
