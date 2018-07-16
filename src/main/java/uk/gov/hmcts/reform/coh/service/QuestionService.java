package uk.gov.hmcts.reform.coh.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.controller.exceptions.NotAValidUpdateException;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Component
public class QuestionService {
    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);


    private QuestionRoundService questionRoundService;
    private QuestionRepository questionRepository;
    private final QuestionStateService questionStateService;


    @Autowired
    public QuestionService(QuestionRepository questionRepository, QuestionStateService questionStateService,
                           QuestionRoundService questionRoundService) {
        this.questionRepository = questionRepository;
        this.questionStateService = questionStateService;
        this.questionRoundService = questionRoundService;
    }

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

    public Question createQuestion(final Question question, OnlineHearing onlineHearing) {

        if(!questionRoundService.isQrValidTransition(question, onlineHearing)) {
            throw new NotAValidUpdateException();
        }

        if(!questionRoundService.isQrValidState(question, onlineHearing)) {
            throw new NotAValidUpdateException();
        }

        Optional<QuestionState> state = questionStateService.retrieveQuestionStateByStateName(QuestionStates.DRAFTED.getStateName());
        if (!state.isPresent()) {
            throw new EntityNotFoundException("Question state not found");
        }
        question.setOnlineHearing(onlineHearing);
        question.setQuestionState(state.get());
        question.updateQuestionStateHistory(state.get());

        return questionRepository.save(question);
    }

    public Question editQuestion(UUID questionId, Question body) {
        Optional<Question> optionalQuestion = retrieveQuestionById(questionId);
        if (!optionalQuestion.isPresent()) {
            throw new EntityNotFoundException("Question entity not found");
        }
        Question question = optionalQuestion.get();
        Optional<QuestionState> state = questionStateService.retrieveQuestionStateByStateName(QuestionStates.ISSUED.getStateName());
        if (!state.isPresent()) {
            throw new EntityNotFoundException("Question state not found");
        }

        question.setQuestionState(state.get());
        question.updateQuestionStateHistory(state.get());

        return questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Question question) {
        questionRepository.delete(question);
    }

    public Question updateQuestion(Question currentQuestion, Question updateToQuestion){
        QuestionState proposedState = updateToQuestion.getQuestionState();
        Optional<QuestionState> issuedState = questionStateService.retrieveQuestionStateByStateName(QuestionStates.ISSUED.getStateName());

        if (!issuedState.isPresent()) {
            throw new EntityNotFoundException("Unable to find state '" + QuestionStates.ISSUED.getStateName() + "'");
        }

        if(proposedState.equals(issuedState.get())) {
            throw new NotAValidUpdateException();
        }

        questionRepository.save(currentQuestion);
        return currentQuestion;
    }

    public Optional<List<Question>> finaAllQuestionsByOnlineHearing(OnlineHearing onlineHearing) {
        return Optional.ofNullable(questionRepository.findAllByOnlineHearing(onlineHearing));
    }
}
