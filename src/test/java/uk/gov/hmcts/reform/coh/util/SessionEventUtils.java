package uk.gov.hmcts.reform.coh.util;

import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.events.EventTypes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class SessionEventUtils {

    private static Map<EventTypes, SessionEventType> mappings;

    static {
        mappings = new HashMap<>();
        Arrays.stream(EventTypes.values()).forEach(et -> mappings.put(et, new SessionEventType(mappings.size() + 1, et.getEventType())));
    }

    private SessionEventUtils() {
    }

    public static final SessionEventType get(EventTypes eventTypes) {
        return mappings.get(eventTypes);
    }
}
