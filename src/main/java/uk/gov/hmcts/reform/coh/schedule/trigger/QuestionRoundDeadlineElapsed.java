package uk.gov.hmcts.reform.coh.schedule.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.QuestionService;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static uk.gov.hmcts.reform.coh.states.QuestionStates.*;

@Component
public class QuestionRoundDeadlineElapsed extends AbstractQuestionRoundEventTrigger {

    private static final Logger log = LoggerFactory.getLogger(QuestionRoundDeadlineElapsed.class);

    @Autowired
    private QuestionService questionService;

    @Override
    public List<Question> getQuestions() {
        log.info("Executing {}", this.getClass());

        Calendar calendar = new GregorianCalendar();

        QuestionState issuedState = getQuestionStateByStateName(ISSUED);
        QuestionState grantedState = getQuestionStateByStateName(QUESTION_DEADLINE_EXTENSION_GRANTED);
        QuestionState elapsedState = getQuestionStateByStateName(DEADLINE_ELAPSED);

        // For each question, update the state to elapsed
        List<Question> questions = questionService.retrieveQuestionsDeadlineExpiredAndQuestionStates(calendar.getTime(), Arrays.asList(issuedState, grantedState));
        questions.forEach(q -> {
            q.setQuestionState(elapsedState);
            questionService.updateQuestionForced(q);
            log.info("Updated question {} to {}", q.getQuestionId(), elapsedState);
        });

        return questions;
    }

    @Override
    public EventTypes getEventType() {
        return EventTypes.QUESTION_DEADLINE_ELAPSED;
    }

    @Override
    public int order() {
        return 10;
    }
}