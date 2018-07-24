package uk.gov.hmcts.reform.coh.schedule.event;

import uk.gov.hmcts.reform.coh.Notification.NotificationRequest;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;

public class QuestionRoundEvent implements EventInterface<OnlineHearing> {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public NotificationRequest transform(OnlineHearing onlineHearing) {

        return null;
    }
}
