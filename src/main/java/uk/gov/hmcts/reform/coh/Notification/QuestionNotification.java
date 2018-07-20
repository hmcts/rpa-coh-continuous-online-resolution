package uk.gov.hmcts.reform.coh.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.domain.EventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;

@Component
public class QuestionNotification {
    private static final Logger log = LoggerFactory.getLogger(QuestionNotification.class);

    private RestTemplate restTemplate;

    public QuestionNotification(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }
    public QuestionNotification(){
        this.restTemplate = new RestTemplate();
    }

    public boolean notifyQuestionState(EventForwardingRegister eventForwardingRegister, OnlineHearing onlineHearing){
        ResponseEntity responseEntity = restTemplate.postForEntity(eventForwardingRegister.getForwardingEndpoint(), onlineHearing, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()){
            return true;
        }else {
            return false;
        }
    }
}
