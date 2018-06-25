package uk.gov.hmcts.reform.coh.Notification;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.Question;

@Component
public class QuestionNotification {

    private RestTemplate restTemplate;

    public QuestionNotification(){
        this.restTemplate = new RestTemplate();
    }

    public boolean notifyQuestionState(Question question){
        ResponseEntity responseEntity = notifyJurisdiction(question);
        if (responseEntity.getStatusCode().is2xxSuccessful()){
            return true;
        }else {
            return false;
        }
    }

    private ResponseEntity notifyJurisdiction(Question question){
        Jurisdiction jurisdiction = question.getOnlineHearing().getJurisdiction();

        if(jurisdiction.getUrl()==null || StringUtils.isEmpty(jurisdiction.getUrl())){
            throw new NullPointerException("No Jurisdiction found for online hearing: " + question.getOnlineHearing().getOnlineHearingId());
        }

        System.out.println("Online hearing Jurisdiction is " + jurisdiction.getJurisdictionName() +
                " and the registered 'issuer' endpoint is " + jurisdiction.getUrl());

        return restTemplate.postForEntity(jurisdiction.getUrl(), question, String.class);
    }
}
