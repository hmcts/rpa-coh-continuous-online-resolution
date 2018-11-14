package uk.gov.hmcts.reform.coh.schedule.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.QuestionService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static uk.gov.hmcts.reform.coh.states.QuestionStates.ISSUED;
import static uk.gov.hmcts.reform.coh.states.QuestionStates.QUESTION_DEADLINE_EXTENSION_GRANTED;

@Component
public class QuestionRoundReminder extends AbstractQuestionRoundEventTrigger {

    private static final Logger log = LoggerFactory.getLogger(QuestionRoundReminder.class);

    @Autowired
    private QuestionService questionService;

    @Override
    public List<Question> getQuestions() {
        log.info("Executing {}", this.getClass());

        LocalDateTime start = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay().plusHours(1);

        QuestionState issuedState = getQuestionStateByStateName(ISSUED);
        QuestionState grantedState = getQuestionStateByStateName(QUESTION_DEADLINE_EXTENSION_GRANTED);

        log.info("Looking questions about to expire between '{}' and '{}'", start, end);

        // Get questions about to expire tomorrow
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