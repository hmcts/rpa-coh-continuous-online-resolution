package uk.gov.hmcts.reform.coh.schedule.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.service.QuestionStateService;
import uk.gov.hmcts.reform.coh.service.SessionEventService;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static uk.gov.hmcts.reform.coh.states.QuestionStates.ISSUED;
import static uk.gov.hmcts.reform.coh.states.QuestionStates.QUESTION_DEADLINE_EXTENSION_GRANTED;

@Component
public class QuestionRoundReminder extends AbstractQuestionRoundEventTrigger {

    private static final Logger log = LoggerFactory.getLogger(QuestionRoundReminder.class);

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionStateService stateService;

    @Autowired
    private SessionEventService sessionEventService;

    @Override
    public List<Question> getQuestions() {
        log.info(String.format("Executing %s", this.getClass()));

        Calendar today = new GregorianCalendar();
        Calendar tomorrow = new GregorianCalendar();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        QuestionState issuedState = getQuestionStateByStateName(ISSUED);
        QuestionState grantedState = getQuestionStateByStateName(QUESTION_DEADLINE_EXTENSION_GRANTED);

        // For each question, update the state to elapsed
        List<Question> questions = questionService.retrieveQuestionsDeadlineExpiredBetweenAndQuestionStates(today.getTime(), tomorrow.getTime(), Arrays.asList(issuedState,  grantedState));

        return questions;
    }

    @Override
    public EventTypes getEventType() {
        return EventTypes.QUESTION_DEADLINE_REMINDER;
    }

    @Override
    public int order() {
        return 20;
    }
}