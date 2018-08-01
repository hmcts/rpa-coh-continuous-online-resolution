package uk.gov.hmcts.reform.coh.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.controller.exceptions.NotAValidUpdateException;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;
import uk.gov.hmcts.reform.coh.service.exceptions.NoQuestionsAsked;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;

import static uk.gov.hmcts.reform.coh.states.QuestionStates.DRAFTED;
import static uk.gov.hmcts.reform.coh.states.QuestionStates.ISSUED;
import static uk.gov.hmcts.reform.coh.states.QuestionStates.QUESTION_DEADLINE_EXTENSION_DENIED;
import static uk.gov.hmcts.reform.coh.states.QuestionStates.QUESTION_DEADLINE_EXTENSION_GRANTED;

@Service
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);

    private QuestionRoundService questionRoundService;

    private QuestionRepository questionRepository;

    private final QuestionStateService questionStateService;
    private QuestionState issued;
    private QuestionState extensionGranted;
    private QuestionState extensionDenied;
    private Duration extension = Duration.ofDays(7);

    @Autowired
    public QuestionService(QuestionRepository questionRepository, QuestionStateService questionStateService,
                           QuestionRoundService questionRoundService) {
        this.questionRepository = questionRepository;
        this.questionStateService = questionStateService;
        this.questionRoundService = questionRoundService;
    }

    @Transactional
    public Question createQuestion(final Question question, OnlineHearing onlineHearing) {
        if(!questionRoundService.isQrValidTransition(question, onlineHearing)
                || !questionRoundService.isQrValidState(question, onlineHearing)) {
            throw new NotAValidUpdateException("Invalid question round transition");
        }

        QuestionState state = questionStateService.retrieveQuestionStateByStateName(DRAFTED.getStateName())
                .orElseThrow(() -> new EntityNotFoundException("Question state not found"));

        question.setOnlineHearing(onlineHearing);
        question.setQuestionState(state);
        question.updateQuestionStateHistory(state);

        return questionRepository.save(question);
    }

    @Transactional
    public Optional<Question> retrieveQuestionById(final UUID question_id){
        Optional<Question> question = questionRepository.findById(question_id);

        if (!question.isPresent()) {
            return question;
        }
        question.get().setQuestionStateHistories(
                question.get().getQuestionStateHistories().stream().sorted(
                        (a, b) -> (a.getDateOccurred().compareTo(b.getDateOccurred()))).collect(Collectors.toList()
                ));

        return question;
    }

    @Transactional
    public Optional<List<Question>> findAllQuestionsByOnlineHearing(OnlineHearing onlineHearing) {
        return Optional.ofNullable(questionRepository.findAllByOnlineHearing(onlineHearing));
    }

    @Transactional
    public List<Question> retrieveQuestionsDeadlineExpiredAndQuestionState(Date threshold, QuestionState questionState) {

        return questionRepository.findAllByDeadlineExpiryDateLessThanEqualAndQuestionState(threshold, questionState);
    }

    @Transactional
    public void updateQuestion(Question question){
        Optional<QuestionState> draftedState = questionStateService.retrieveQuestionStateByStateName(DRAFTED.getStateName());
        if (!draftedState.isPresent()) {
            throw new EntityNotFoundException(String.format("Question state not found: %s", DRAFTED.getStateName()));
        }

        if (!question.getQuestionState().equals(draftedState.get())) {
            throw new NotAValidUpdateException("Cannot update a question not in draft or pending state");
        }

        questionRepository.save(question);
    }

    @Transactional
    public Question updateQuestionForced(Question question){

        return questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Question question) {
        questionRepository.delete(question);
    }

    @Transactional
    public void requestDeadlineExtension(OnlineHearing onlineHearing) throws NoQuestionsAsked {
        List<Question> questions = findAllQuestionsByOnlineHearing(onlineHearing)
            .orElseThrow(() -> new RuntimeException("Could not retrieve questions"));

        if (questions.isEmpty()) {
            throw new NoQuestionsAsked();
        }

        issued = questionStateService.fetchQuestionState(ISSUED);
        extensionGranted = questionStateService.fetchQuestionState(QUESTION_DEADLINE_EXTENSION_GRANTED);
        extensionDenied = questionStateService.fetchQuestionState(QUESTION_DEADLINE_EXTENSION_DENIED);

        Instant now = Instant.now();

        questions.stream()
            .filter(question -> now.isBefore(question.getDeadlineExpiryDate().toInstant()))
            .forEach(question -> {
                if (shouldGrantExtensionTo(question)) {
                    grantDeadlineExtension(question);
                } else if (shouldDenyExtensionTo(question)) {
                    denyDeadlineExtension(question);
                }
            });
    }

    private boolean shouldGrantExtensionTo(Question question) {
        return issued.equals(question.getQuestionState());
    }

    private boolean shouldDenyExtensionTo(Question question) {
        return extensionGranted.equals(question.getQuestionState());
    }

    private void grantDeadlineExtension(Question question) {
        question.setDeadlineExpiryDate(newDeadline(question));
        question.setQuestionState(extensionGranted);
        question.updateQuestionStateHistory(extensionGranted);
        questionRepository.save(question);
    }

    private Date newDeadline(Question question) {
        Temporal temporal = extension.addTo(question.getDeadlineExpiryDate().toInstant());
        return Date.from(Instant.from(temporal));
    }

    private void denyDeadlineExtension(Question question) {
        question.setQuestionState(extensionDenied);
        question.updateQuestionStateHistory(extensionDenied);
        questionRepository.save(question);
    }
}
