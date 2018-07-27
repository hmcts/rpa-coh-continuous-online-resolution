package uk.gov.hmcts.reform.coh.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.states.OnlineHearingStates;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.states.AnswerStates;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Service
public class AnswersReceivedTask implements ContinuousOnlineResolutionTask<OnlineHearing>{

    private static final Logger log = LoggerFactory.getLogger(AnswersReceivedTask.class);

    private OnlineHearingService onlineHearingService;

    private OnlineHearingStateService onlineHearingStateService;

    private QuestionService questionService;

    private AnswerService answerService;

    @Autowired
    public AnswersReceivedTask(OnlineHearingService onlineHearingService, OnlineHearingStateService onlineHearingStateService, QuestionService questionService, AnswerService answerService) {
        this.onlineHearingService = onlineHearingService;
        this.onlineHearingStateService = onlineHearingStateService;
        this.questionService = questionService;
        this.answerService = answerService;
    }

    public void execute(OnlineHearing onlineHearing) {

        log.debug(format("AnswersReceivedTask.execute(). For online hearing: %s", onlineHearing.getOnlineHearingId()));
        Optional<List<Question>> questions = questionService.findAllQuestionsByOnlineHearing(onlineHearing);
        log.debug(format("AnswersReceivedTask.execute(). OnlineHearing has questions: %s", (questions.isPresent() ? questions.get().size() : "0")));
        if (!questions.isPresent()) {
            return;
        }

        for (Question question : questions.get()) {
            if (!questionContainsAnAnswer(question)) {
                log.debug(format("AnswersReceivedTask.execute(). Question %s does not have a submitted answer",  question.getQuestionId()));
                return;
            }
        }

        // If we get this far, then all questions have been answered
        String nextState = OnlineHearingStates.ANSWERS_SENT.getStateName();
        log.debug(format("AnswersReceivedTask.execute(). Expected next state: %s", nextState));
        Optional<OnlineHearingState> state = onlineHearingStateService.retrieveOnlineHearingStateByState(nextState);
        if (!state.isPresent()) {
            log.debug(format("AnswersReceivedTask.execute(). Failed to update online hearing after all questions have been answered. Unable to retrieve %s", nextState));
            return;
        }

        onlineHearing.setOnlineHearingState(state.get());
        onlineHearing.registerStateChange();
        onlineHearingService.updateOnlineHearing(onlineHearing);
        log.debug("Online hearing updated to indicate all questions answered");
    }

    public boolean questionContainsAnAnswer(Question question) {

        List<Answer> answers = answerService.retrieveAnswersByQuestion(question);
        if (!answers.isEmpty()) {
            // For MVP, there should only be one answer
            log.debug(format("AnswersReceivedTask.questionContainsAnAnswer(). Status of answer %s is %s", answers.get(0).getAnswerId(), answers.get(0).getAnswerState().getState()));
            return answers.get(0).getAnswerState().getState().equals(AnswerStates.SUBMITTED.getStateName());
        }

        return false;
    }
}
