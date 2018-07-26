package uk.gov.hmcts.reform.coh.schedule.notifiers;

import java.util.Arrays;
import java.util.List;

public interface EventTransformer<T> {

    NotificationRequest transform(T t);

    default List<String> supports() {
        return Arrays.asList("default");
    }

}