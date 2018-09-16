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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
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

        LocalDateTime start = LocalDate.now().plus(1, ChronoUnit.DAYS).atStartOfDay();
        LocalDateTime end = LocalDate.now().plus(1, ChronoUnit.DAYS).atStartOfDay().plus(1, ChronoUnit.HOURS);

        QuestionState issuedState = getQuestionStateByStateName(ISSUED);
        QuestionState grantedState = getQuestionStateByStateName(QUESTION_DEADLINE_EXTENSION_GRANTED);

        log.info(String.format("Looking questions about to expire between '%s' and '%s'", start, end));

        // For each question, update the state to elapsed
        List<Question> questions = questionService.retrieveQuestionsDeadlineExpiredBetweenAndQuestionStates(
                convertToLocalDateToDate(start),
                convertToLocalDateToDate(end), Arrays.asList(issuedState,  grantedState));

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

    private Date convertToLocalDateToDate(LocalDateTime localDate) {
        return java.sql.Timestamp.valueOf(localDate);
    }
}