package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.Notification.QuestionNotification;
import uk.gov.hmcts.reform.coh.domain.EventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.EventType;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.repository.EventForwardingRegisterRepository;
import uk.gov.hmcts.reform.coh.repository.EventTypeRespository;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class NotificationService {

    private EventForwardingRegisterRepository eventForwardingRegisterRepository;
    private EventTypeRespository eventTypeRespository;
    private QuestionNotification questionNotification;

    @Autowired
    public NotificationService(EventForwardingRegisterRepository eventForwardingRegisterRepository, EventTypeRespository eventTypeRespository, QuestionNotification questionNotification) {
        this.eventForwardingRegisterRepository = eventForwardingRegisterRepository;
        this.eventTypeRespository = eventTypeRespository;
        this.questionNotification = questionNotification;
    }

    public void notifyIssuedQuestionRound(OnlineHearing onlineHearing) {
        Optional<EventType> optionIssuedEventType = eventTypeRespository.findByEventTypeName(EventTypes.QUESTION_ROUND_ISSUED.getStateName());

        if(!optionIssuedEventType.isPresent()) {
            throw new NoSuchElementException("Error: Required event type not found.");
        }

        Optional<EventForwardingRegister> optEventForwardingRegister = eventForwardingRegisterRepository.findByJurisdictionAndEventType(onlineHearing.getJurisdiction(), optionIssuedEventType.get());

        if(!optEventForwardingRegister.isPresent()) {
            throw new NoSuchElementException("Error: No record for notification");
        }

        questionNotification.notifyQuestionState(optEventForwardingRegister.get(), onlineHearing);
    }
}
