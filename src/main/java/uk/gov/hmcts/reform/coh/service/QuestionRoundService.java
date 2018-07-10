package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.Onlinehearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@Component
public class QuestionRoundService {

    private QuestionRepository questionRepository;

    @Autowired
    public QuestionRoundService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public boolean validateQuestionRound(Question question, Onlinehearing onlinehearing) {
        if (question.getQuestionRound() == null || question.getQuestionRound() == 0) {
            throw new EntityNotFoundException();
        }
        Jurisdiction jurisdiction = onlinehearing.getJurisdiction();

        int maxQuestionRounds = jurisdiction.getMaxQuestionRounds();
        int targetQuestionRound = question.getQuestionRound();
        int currentQuestionRound = getQuestionRound(onlinehearing);

        if (currentQuestionRound == 0) {
            return (targetQuestionRound == 1);
        } else if (currentQuestionRound == targetQuestionRound) {
            return true;
        }
        if (isIncremented(targetQuestionRound, currentQuestionRound) && !isMaxRoundLimit(maxQuestionRounds)) {
            return true;
        } else if (isIncremented(targetQuestionRound, currentQuestionRound) && targetQuestionRound <= maxQuestionRounds){
            return true;
        }
        return false;
    }

    protected boolean isIncremented(int targetQuestionRound, int currentQuestionRound) {
        return targetQuestionRound == currentQuestionRound + 1;
    }

    protected boolean isMaxRoundLimit(int maxQuestionRounds) {
        return maxQuestionRounds > 0;
    }

    protected int getQuestionRound(Onlinehearing onlinehearing) {
        List<Question> orderedQuestions = getQuestionsOrderedByRound(onlinehearing);
        if (orderedQuestions.isEmpty()) {
            return 0;
        }
        return orderedQuestions.get(0).getQuestionRound();
    }

    public List<Question> getQuestionsOrderedByRound(Onlinehearing onlinehearing) {
        return questionRepository.findAllByOnlinehearingOrderByQuestionRoundDesc(onlinehearing);
    }
}
