package uk.gov.hmcts.reform.coh.schedule.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.events.EventTypes;

import java.util.Arrays;
import java.util.List;

@Component
public class DecisionIssuedTransformer implements EventTransformer {

    @Override
    public NotificationRequest transform(Object o) {
        return new NotificationRequest();
    }

    @Override
    public List<String> supports() {
        return Arrays.asList(EventTypes.DECISION_ISSUED.getEventType(), "foo");
    }
}
