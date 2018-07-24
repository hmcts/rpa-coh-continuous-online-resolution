package uk.gov.hmcts.reform.coh.schedule.event;

import uk.gov.hmcts.reform.coh.Notification.NotificationRequest;
import uk.gov.hmcts.reform.coh.domain.EventType;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.events.EventTypes;

public class QuestionRoundEvent implements EventInterface<OnlineHearing> {

    private EventTypes eventType = EventTypes.QUESTION_ROUND_ISSUED;

    @Override
    public String getName() {
        return eventType.toString();
    }

    @Override
    public NotificationRequest transform(OnlineHearing onlineHearing) {
        NotificationRequest request = new NotificationRequest();
        request.setCaseId(onlineHearing.getCaseId());
        request.setOnlineHearingId(onlineHearing.getOnlineHearingId());
        request.setEventType(eventType);
        // not sure if this is the right date
        request.setExpiryDate(onlineHearing.getEndDate());
        return request;
    }
}
