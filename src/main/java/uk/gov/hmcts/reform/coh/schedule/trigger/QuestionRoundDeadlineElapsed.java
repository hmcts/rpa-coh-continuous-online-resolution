package uk.gov.hmcts.reform.coh.schedule.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.service.QuestionStateService;
import uk.gov.hmcts.reform.coh.service.SessionEventService;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.coh.states.QuestionStates.DEADLINE_ELAPSED;
import static uk.gov.hmcts.reform.coh.states.QuestionStates.ISSUED;

@Component
public class QuestionRoundDeadlineElapsed implements EventTrigger {

    private static final Logger log = LoggerFactory.getLogger(QuestionRoundDeadlineElapsed.class);

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
        log.info(String.format("Executing %s", this.getClass()));

        Calendar calendar = new GregorianCalendar();
        Optional<QuestionState> issuedState = stateService.retrieveQuestionStateByStateName(ISSUED.getStateName());
        if (!issuedState.isPresent()) {
            log.error(String.format("Unable to find question state: %s", ISSUED.getStateName()));
            return;
        }

        Optional<QuestionState> elapsedState = stateService.retrieveQuestionStateByStateName(DEADLINE_ELAPSED.getStateName());
        if (!elapsedState.isPresent()) {
            log.error(String.format("Unable to find question state: %s", DEADLINE_ELAPSED.getStateName()));
            return;
        }

        // For each question, update the state to elapsed
        List<Question> questions = questionService.retrieveQuestionsDeadlineExpiredAndQuestionState(calendar.getTime(), issuedState.get());
        questions.forEach(q -> {
            q.setQuestionState(elapsedState.get());
            questionService.updateQuestionForced(q);
            log.info(String.format("Updated question %s to %s", q.getQuestionId(), elapsedState));
        });

        // Only create one one session event per online hearing in case there's many rounds
        List<OnlineHearing> onlineHearings = retrieveQuestionsDeadlineExpiredAndQuestionStateDistinct(questions);
        onlineHearings.forEach( o -> {
            Optional<OnlineHearing> onlineHearing = onlineHearingService.retrieveOnlineHearing(o);
            log.info(String.format("Online hearing %s found", o.getOnlineHearingId()));
            if (onlineHearing.isPresent()) {
                sessionEventService.createSessionEvent(onlineHearing.get(), EventTypes.QUESTION_DEADLINE_ELAPSED.getEventType());
                log.info(String.format("Session event created for %s", o.getOnlineHearingId()));
            }
        });
    }

    public List<OnlineHearing> retrieveQuestionsDeadlineExpiredAndQuestionStateDistinct(List<Question> questions) {

        return questions.stream()
                .map(Question::getOnlineHearing)
                .distinct()
                .collect(Collectors.toList());
    }
}