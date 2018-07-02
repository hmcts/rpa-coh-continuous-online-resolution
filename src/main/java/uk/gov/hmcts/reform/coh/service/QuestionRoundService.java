package uk.gov.hmcts.reform.coh.service;

import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;

import java.util.Optional;

@Service
@Component
public class QuestionRoundService {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private OnlineHearingService onlineHearingService;

    public boolean validateQuestionRound(Question question){

        int targetQuestionRound = question.getQuestionRound();
        Optional<Question> questionOptional = questionService.retrieveQuestionById(question.getQuestionId());
        if(!questionOptional.isPresent()){
            return targetQuestionRound == 1;
        }

        int sourceQuestionRound = 0;
        try {
            sourceQuestionRound = getQuestionRound(question);
        } catch (NotFoundException e) {
            System.out.println(e);
        }
        if(sourceQuestionRound == targetQuestionRound){
            return true;
        }

        if(targetQuestionRound < sourceQuestionRound){
            return false;
        }
    }

    private int getQuestionRound(Question question) throws NotFoundException {
        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(question.getOnlineHearing());
        if(!optionalOnlineHearing.isPresent()){
            throw new NotFoundException("Error: No valid online hearing found for question");
        }
        

    }

}
