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
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
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
    public Question createQuestion(final Question question, OnlineHearing onlineHearing) {
        if(!questionRoundService.isQrValidTransition(question, onlineHearing)) {
            throw new NotAValidUpdateException("Invalid question round state transition");
        }

        if(!questionRoundService.isQrValidState(question, onlineHearing)) {
            throw new NotAValidUpdateException("Cannot add question to issued question round");
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

    @Transactional
    public void deleteQuestion(Question question) {
        questionRepository.delete(question);
    }

    @Transactional
    public void updateQuestion(Question question){
        Optional<QuestionState> draftedState = questionStateService.retrieveQuestionStateByStateName(QuestionStates.DRAFTED.getStateName());
        if (!draftedState.isPresent()) {
            throw new EntityNotFoundException("Question state not found");
        }
        if(!question.getQuestionState().equals(draftedState.get())) {
            throw new NotAValidUpdateException("Cannot update a question not in draft state");
        }

        questionRepository.save(question);
    }

    @Transactional
    public Optional<List<Question>> findAllQuestionsByOnlineHearing(OnlineHearing onlineHearing) {
        return Optional.ofNullable(questionRepository.findAllByOnlineHearing(onlineHearing));
    }
}
