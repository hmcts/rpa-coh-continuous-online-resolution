package uk.gov.hmcts.reform.coh.schedule.event;

import uk.gov.hmcts.reform.coh.Notification.NotificationRequest;

public interface EventTransformer<T> {

    NotificationRequest transform(T t);

    String getName();

}