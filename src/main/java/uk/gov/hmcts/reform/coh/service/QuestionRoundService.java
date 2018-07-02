package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@Component
public class QuestionRoundService {

    private QuestionRepository questionRepository;

    @Autowired
    public QuestionRoundService(QuestionRepository questionRepository){
        this.questionRepository = questionRepository;
    }

    public boolean validateQuestionRound(Question question, OnlineHearing onlineHearing){

        if(question.getQuestionRound()==null || question.getQuestionRound()==0){
            throw new EntityNotFoundException();
        }

        Jurisdiction jurisdiction = onlineHearing.getJurisdiction();

        int maxQuestionRounds = jurisdiction.getMaxQuestionRounds();
        int targetQuestionRound = question.getQuestionRound();
        int currentQuestionRound = getQuestionRound(onlineHearing);

        if(currentQuestionRound == targetQuestionRound) {
            return true;
        }else if(targetQuestionRound <= maxQuestionRounds && targetQuestionRound == currentQuestionRound + 1){
            return true;
        }else{
            return false;
        }
    }

    protected int getQuestionRound(OnlineHearing onlineHearing){
        List<Question> orderedQuestions = getQuestionsOrderedByRound(onlineHearing);
        if (orderedQuestions.isEmpty()){
            return 1;
        }
        return orderedQuestions.get(0).getQuestionRound();
    }

    public List<Question> getQuestionsOrderedByRound(OnlineHearing onlineHearing){
        return questionRepository.findAllByOnlineHearingOrderByQuestionRoundDesc(onlineHearing);
    }
}
