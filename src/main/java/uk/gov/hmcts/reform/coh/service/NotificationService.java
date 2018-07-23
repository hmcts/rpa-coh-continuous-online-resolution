package uk.gov.hmcts.reform.coh.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.Notification.Notifier;
import uk.gov.hmcts.reform.coh.controller.exceptions.NotificationException;
import uk.gov.hmcts.reform.coh.domain.EventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.EventForwardingRegisterId;
import uk.gov.hmcts.reform.coh.domain.EventType;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.repository.EventForwardingRegisterRepository;
import uk.gov.hmcts.reform.coh.repository.EventTypeRespository;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private EventForwardingRegisterRepository eventForwardingRegisterRepository;
    private EventTypeRespository eventTypeRespository;
    private Notifier notifier;

    @Autowired
    public NotificationService(EventForwardingRegisterRepository eventForwardingRegisterRepository, EventTypeRespository eventTypeRespository, Notifier notifier) {
        this.eventForwardingRegisterRepository = eventForwardingRegisterRepository;
        this.eventTypeRespository = eventTypeRespository;
        this.notifier = notifier;
    }

    public boolean notifyIssuedQuestionRound(OnlineHearing onlineHearing) {
        Optional<EventType> optionIssuedEventType = eventTypeRespository.findByEventTypeName(EventTypes.QUESTION_ROUND_ISSUED.getStateName());
        if(!optionIssuedEventType.isPresent()) {
            throw new NoSuchElementException("Error: Required event type not found.");
        }

        Optional<EventForwardingRegister> optEventForwardingRegister = eventForwardingRegisterRepository.findById(
                new EventForwardingRegisterId(onlineHearing.getJurisdiction().getJurisdictionId(), optionIssuedEventType.get().getEventTypeId()));
        if(!optEventForwardingRegister.isPresent()) {
            throw new NoSuchElementException("Error: No record for notification");
        }

        log.info("Event forwarding register: " + optEventForwardingRegister.get().toString());

        try {
            return notifier.notifyQuestionsIssued(optEventForwardingRegister.get(), onlineHearing);
        }catch (Exception e) {
            log.error("Exception when notifying: " + optEventForwardingRegister.get().toString() + ":" + e);
            throw new NotificationException("Failed to notify: " + optEventForwardingRegister.get().getForwardingEndpoint() + ":" + e);
        }
    }
}
