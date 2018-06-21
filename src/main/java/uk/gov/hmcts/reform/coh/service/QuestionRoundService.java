package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;
import uk.gov.hmcts.reform.coh.repository.QuestionRoundRepository;

import java.util.Optional;

@Service
@Component
public class QuestionRoundService {

    private RestTemplate restTemplate;
    private JurisdictionRepository jurisdictionRepository;
    private QuestionRoundRepository questionRoundRepository;

    @Autowired
    public QuestionRoundService(JurisdictionRepository jurisdictionRepository, QuestionRoundRepository questionRoundRepository) {
        this.jurisdictionRepository = jurisdictionRepository;
        this.questionRoundRepository = questionRoundRepository;
    }

    public Optional<QuestionRound> getQuestionRound(Integer roundId) {
        return questionRoundRepository.findById(roundId);
    }

    public Boolean setStateToIssued(QuestionRound questionRound) {
        Jurisdiction jurisdiction = questionRound.getOnlineHearing().getJurisdiction();
        if(jurisdiction==null){
            throw new NullPointerException("No Jurisdiction found for online hearing: " + questionRound.getOnlineHearing().getOnlineHearingId());
        }

        System.out.println("Online hearing Jurisdiction is " + jurisdiction.getJurisdictionName() +
                " and the registered 'issuer' endpoint is " + jurisdiction.getUrl() +
                " sending request for question round id " + questionRound.getQuestionRoundId());

        boolean success = notifyJurisdiction(jurisdiction, questionRound);
        if(success){
            questionRoundRepository.save(questionRound);
            System.out.println("Successfully issued question round and sent notification to jurisdiction");
            return true;
        }else{
            System.out.println("Request to jurisdiction was unsuccessful");
            return false;
        }
    }

    protected boolean notifyJurisdiction(Jurisdiction jurisdiction, QuestionRound questionRound) throws HttpStatusCodeException{
        restTemplate = new RestTemplate();
        try {
            ResponseEntity responseEntity = restTemplate.postForEntity(jurisdiction.getUrl(), "Online hearing id: " +
                    questionRound.getOnlineHearing().getOnlineHearingId() + " - Notification - Question round issued: " +
                    questionRound.getQuestionRoundId(), String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()){
                QuestionState questionState = new QuestionState();
                questionState.setQuestionStateId(3);
                questionRound.setQuestionState(questionState);
                return true;
            }else {
                return false;
            }

        }catch(HttpStatusCodeException e){
            throw e;
        }
    }
}
