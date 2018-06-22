package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import uk.gov.hmcts.reform.coh.Notification.QuestionRoundDespatcher;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionRoundRepository;
import uk.gov.hmcts.reform.coh.repository.QuestionStateRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@Component
public class QuestionRoundService {

    private QuestionRoundRepository questionRoundRepository;
    private QuestionRoundDespatcher questionRoundDespatcher;
    private QuestionStateRepository questionStateRepository;

    @Autowired
    public QuestionRoundService(QuestionRoundRepository questionRoundRepository, QuestionRoundDespatcher questionRoundDespatcher, QuestionStateRepository questionStateRepository) {
        this.questionRoundRepository = questionRoundRepository;
        this.questionRoundDespatcher = questionRoundDespatcher;
        this.questionStateRepository = questionStateRepository;
    }

    public Optional<QuestionRound> getQuestionRound(UUID roundId) {
        return questionRoundRepository.findById(roundId);
    }

    public Boolean notifyJurisdictionToIssued(QuestionRound questionRound) {
        Jurisdiction jurisdiction = questionRound.getOnlineHearing().getJurisdiction();
        if(jurisdiction==null){
            throw new NullPointerException("No Jurisdiction found for online hearing: " + questionRound.getOnlineHearing().getOnlineHearingId());
        }

        System.out.println("Online hearing Jurisdiction is " + jurisdiction.getJurisdictionName() +
                " and the registered 'issuer' endpoint is " + jurisdiction.getUrl() +
                " sending request for question round id " + questionRound.getQuestionRoundId());

        boolean success = setStateToIssued(jurisdiction, questionRound);
        //boolean success = true;
        if(success){
            questionRoundRepository.save(questionRound);
            System.out.println("Successfully issued question round and sent notification to jurisdiction");
            return true;
        }else{
            System.out.println("Request to jurisdiction was unsuccessful");
            return false;
        }
    }

    protected boolean setStateToIssued(Jurisdiction jurisdiction, QuestionRound questionRound) throws ResourceAccessException{
        try {
            ResponseEntity responseEntity = questionRoundDespatcher.sendRequestToJuridiction(jurisdiction, questionRound);
            if (responseEntity.getStatusCode().is2xxSuccessful()){
                Optional<QuestionState> optQuestionState = questionStateRepository.findById(3);
                QuestionState questionState = optQuestionState.get();
                questionState.setQuestionStateId(3);
                questionRound.setQuestionState(questionState);
                return true;
            }else {
                return false;
            }

        }catch(ResourceAccessException e){
            throw e;
        }
    }
}
