package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;
import uk.gov.hmcts.reform.coh.repository.QuestionRoundRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Component
public class QuestionRoundService {

    private RestTemplate restTemplate;

    private JurisdictionRepository jurisdictionRepository;
    private OnlineHearingRepository onlineHearingRepository;
    private QuestionRoundRepository questionRoundRepository;

    @Autowired
    public QuestionRoundService(JurisdictionRepository jurisdictionRepository, OnlineHearingRepository onlineHearingRepository, QuestionRoundRepository questionRoundRepository) {
        this.onlineHearingRepository = onlineHearingRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.questionRoundRepository = questionRoundRepository;
    }

    public QuestionRound issueQuestions(String external_ref, Integer round_id) {
        Jurisdiction jurisdiction = getJurisdiction(external_ref);

        QuestionRound questionRound = getQuestionRound(round_id);

        System.out.println("Online hearing Jurisdiction is " + jurisdiction.getJurisdictionName() +
                " and the registered 'issuer' endpoint is " + jurisdiction.getUrl() +
                " sending request for question round id " + questionRound.getQuestionRoundId());

        boolean success = issueQuestionRound(jurisdiction, external_ref);
        if(success){
            questionRound.setState_id(3);
            questionRoundRepository.save(questionRound);
            System.out.println("Successfully updated state to issued");
            return questionRound;
        }else{
            System.out.println("No update has been made");
            return questionRound;
        }
    }

    public QuestionRound getQuestionRound(Integer round_id){
        Optional<QuestionRound> optQuestionRound = questionRoundRepository.findById(round_id);
        if(!optQuestionRound.isPresent()){
            throw new NoSuchElementException("Question round not found");
        }
        return optQuestionRound.get();
    }

    public Jurisdiction getJurisdiction(String external_ref){
        Optional<OnlineHearing> onlineHearing = onlineHearingRepository.findByExternalRef(external_ref);
        if(!onlineHearing.isPresent()) {
            throw new NoSuchElementException("Online hearing not found");
        }

        Optional<Jurisdiction> optJurisdiction = jurisdictionRepository.findById(onlineHearing.get().getJurisdictionId());
        if(!optJurisdiction.isPresent()){
            throw new NoSuchElementException("Jurisdiction not found");
        }
        return optJurisdiction.get();
    }

    protected boolean issueQuestionRound(Jurisdiction jurisdiction, String external_ref) throws HttpStatusCodeException{
        restTemplate = new RestTemplate();
        try {
            /*
            ResponseEntity responseEntity = restTemplate.postForEntity(jurisdiction.getUrl(), "Case Id: " + external_ref + " - Notification - Question issued", String.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()){
                return true;
            }else {
                return false;
            }*/
            return true;

        }catch(HttpStatusCodeException e){
            throw e;
        }
    }
}
