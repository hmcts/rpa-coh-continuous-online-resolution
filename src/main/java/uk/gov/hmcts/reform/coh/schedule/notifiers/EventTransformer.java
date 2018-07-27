package uk.gov.hmcts.reform.coh.schedule.notifiers;

import uk.gov.hmcts.reform.coh.domain.SessionEvent;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;

import java.util.Arrays;
import java.util.List;

public interface EventTransformer<T> {

    NotificationRequest transform(SessionEventType sessionEventType, T t);

    default List<String> supports() {
        return Arrays.asList("default");
    }

}