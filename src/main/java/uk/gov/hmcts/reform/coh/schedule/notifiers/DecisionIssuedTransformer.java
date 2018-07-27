package uk.gov.hmcts.reform.coh.schedule.notifiers;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.DecisionService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class DecisionIssuedTransformer implements EventTransformer<OnlineHearing> {

    private static final ISO8601DateFormat df = new ISO8601DateFormat();

    @Autowired
    private DecisionService decisionService;

    @Override
    public NotificationRequest transform(SessionEventType sessionEventType, OnlineHearing onlineHearing) {

        Optional<Decision> optDecision = decisionService.findByOnlineHearingId(onlineHearing.getOnlineHearingId());
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setCaseId(onlineHearing.getCaseId());
        notificationRequest.setOnlineHearingId(onlineHearing.getOnlineHearingId());
        notificationRequest.setEventType(sessionEventType.getEventTypeName());
        notificationRequest.setExpiryDate(df.format(optDecision.get().getDeadlineExpiryDate()));

        return notificationRequest;
    }

    @Override
    public List<String> supports() {
        return Arrays.asList(EventTypes.DECISION_ISSUED.getEventType());
    }
}
