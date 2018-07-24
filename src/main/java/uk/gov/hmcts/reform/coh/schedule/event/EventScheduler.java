package uk.gov.hmcts.reform.coh.schedule.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.Notification.NotificationRequest;
import uk.gov.hmcts.reform.coh.domain.EventType;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.events.EventTypes;

import java.awt.*;

@Component
public class EventScheduler {

    private EventType eventType;

    @Autowired
    private EventInterfaceManager eventInterfaceManager;

    public EventScheduler(EventType eventType) {
        this.eventType = eventType;;
    }

    public boolean sendNotification() {
        return false;
    }

    private NotificationRequest constructRequest(OnlineHearing onlineHearing, EventTypes eventType) {
        EventInterface eventInterface = eventInterfaceManager.getInterface(eventType.toString());
        return eventInterface.transform(onlineHearing);
    }

}
