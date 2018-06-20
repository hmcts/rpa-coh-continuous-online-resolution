package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
public class JurisdictionService {

    private RestTemplate restTemplate;

    private JurisdictionRepository jurisdictionRepository;
    private OnlineHearingRepository onlineHearingRepository;
    private QuestionRoundRepository questionRoundRepository;


    @Autowired
    public JurisdictionService(JurisdictionRepository jurisdictionRepository, OnlineHearingRepository onlineHearingRepository) {
        this.onlineHearingRepository = onlineHearingRepository;
        this.jurisdictionRepository = jurisdictionRepository;
    }

    public QuestionRound issueQuestions(String external_ref, Integer round_id) {
        Jurisdiction jurisdiction = getJurisdiction(external_ref);


        QuestionRound questionRound = new QuestionRound();

        System.out.println("Online hearing Jurisdiction is " + jurisdiction.getJurisdictionName() +
                " and the registered 'issuer' endpoint is " + jurisdiction.getUrl());
        boolean success = issueQuestionRound(jurisdiction, external_ref);

        return questionRound;
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
            ResponseEntity responseEntity = restTemplate.postForEntity(jurisdiction.getUrl(), "Case Id: " + external_ref + " - Notification - Question issued", String.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()){
                return true;
            }else {
                return false;
            }
        }catch(HttpStatusCodeException e){
            throw e;
        }
    }
}
