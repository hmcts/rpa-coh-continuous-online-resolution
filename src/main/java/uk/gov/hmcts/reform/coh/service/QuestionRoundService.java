package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import javax.persistence.EntityNotFoundException;
import java.util.*;

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

        Optional<Integer> maxQuestionRounds = jurisdiction.getMaxQuestionRounds();
        if(!maxQuestionRounds.isPresent() || maxQuestionRounds.get()==0){
            return true;
        }
        int targetQuestionRound = question.getQuestionRound();
        int currentQuestionRound = getQuestionRoundNumber(onlineHearing);

        if(currentQuestionRound == 0){
            return targetQuestionRound == 1;
        }else if(currentQuestionRound == targetQuestionRound) {
            return true;
        }else if(targetQuestionRound <= maxQuestionRounds.get() && targetQuestionRound == currentQuestionRound + 1){
            return true;
        }else{
            return false;
        }
    }

    public List<QuestionRound> getAllQuestionRounds(OnlineHearing onlineHearing){

        List<QuestionRound> questionRounds = new ArrayList<>();

        for(int questionRoundNumber = 1; questionRoundNumber <= getQuestionRoundNumber(onlineHearing); questionRoundNumber++){
            QuestionRound questionRound = new QuestionRound();
            questionRound.setQuestionRoundNumber(questionRoundNumber);
            questionRound.setGetQuestionList(questionRepository.findByOnlineHearingAndQuestionRound(onlineHearing, questionRoundNumber));

            questionRounds.add(questionRound);
        }

        return questionRounds;
    }

    protected Integer getQuestionRoundNumber(OnlineHearing onlineHearing){
        List<Question> orderedQuestions = getQuestionsOrderedByRound(onlineHearing);
        if (orderedQuestions.isEmpty()){
            return 0;
        }
        return orderedQuestions.get(0).getQuestionRound();
    }

    public List<Question> getQuestionsOrderedByRound(OnlineHearing onlineHearing){
        return questionRepository.findAllByOnlineHearingOrderByQuestionRoundDesc(onlineHearing);
    }
}
