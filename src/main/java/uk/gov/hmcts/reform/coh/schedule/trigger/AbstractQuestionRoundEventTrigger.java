package uk.gov.hmcts.reform.coh.schedule.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.service.QuestionStateService;
import uk.gov.hmcts.reform.coh.service.SessionEventService;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
abstract public class AbstractQuestionRoundEventTrigger implements EventTrigger {

    private static final Logger log = LoggerFactory.getLogger(AbstractQuestionRoundEventTrigger.class);

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionStateService stateService;

    @Autowired
    private SessionEventService sessionEventService;

    @Override
    public void execute() {
        // Only create one one session event per online hearing in case there's many rounds
        List<OnlineHearing> onlineHearings = retrieveQuestionsDeadlineExpiredAndQuestionStateDistinct(getQuestions());
        onlineHearings.forEach(o -> {
            Optional<OnlineHearing> onlineHearing = onlineHearingService.retrieveOnlineHearing(o);
            log.info("Online hearing {} found", o.getOnlineHearingId());
            if (onlineHearing.isPresent()) {
                sessionEventService.createSessionEvent(onlineHearing.get(), getEventType().getEventType());
                log.info("Session event created for {}", o.getOnlineHearingId());
            }
        });
    }

    protected List<OnlineHearing> retrieveQuestionsDeadlineExpiredAndQuestionStateDistinct(List<Question> questions) {

        return questions.stream()
                .map(Question::getOnlineHearing)
                .distinct()
                .collect(Collectors.toList());
    }

    protected QuestionState getQuestionStateByStateName(QuestionStates state) throws EntityNotFoundException {
        Optional<QuestionState> questionState = stateService.retrieveQuestionStateByStateName(state.getStateName());
        return questionState.orElseThrow(() -> new EntityNotFoundException("Unable to find question state: " + state.getStateName()));
    }

    abstract public List<Question> getQuestions();

    abstract public EventTypes getEventType();
}