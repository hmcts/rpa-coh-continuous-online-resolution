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
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.coh.states.QuestionStates.*;

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

        QuestionState issuedState = getQuestionStateByStateName(ISSUED);
        QuestionState grantedState = getQuestionStateByStateName(QUESTION_DEADLINE_EXTENSION_GRANTED);
        QuestionState elapsedState = getQuestionStateByStateName(DEADLINE_ELAPSED);

        // For each question, update the state to elapsed
        List<Question> questions = questionService.retrieveQuestionsDeadlineExpiredAndQuestionStates(calendar.getTime(), Arrays.asList(issuedState, grantedState));
        questions.forEach(q -> {
            q.setQuestionState(elapsedState);
            questionService.updateQuestionForced(q);
            log.info(String.format("Updated question %s to %s", q.getQuestionId(), elapsedState));
        });

        // Only create one one session event per online hearing in case there's many rounds
        List<OnlineHearing> onlineHearings = retrieveQuestionsDeadlineExpiredAndQuestionStateDistinct(questions);
        onlineHearings.forEach(o -> {
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

    private QuestionState getQuestionStateByStateName(QuestionStates state) throws EntityNotFoundException {
        Optional<QuestionState> questionState = stateService.retrieveQuestionStateByStateName(state.getStateName());
        return questionState.orElseThrow(() -> new EntityNotFoundException("Unable to find question state: " + state.getStateName()));
    }
}