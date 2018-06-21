package uk.gov.hmcts.reform.coh.Notification;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;

@Component
public class QuestionRoundDespatcher {

    private RestTemplate restTemplate;

    public QuestionRoundDespatcher(){
        this.restTemplate = new RestTemplate();
    }

    public ResponseEntity sendRequestToJuridiction(Jurisdiction jurisdiction, QuestionRound questionRound){
        return restTemplate.postForEntity(jurisdiction.getUrl(), "Online hearing id: " +
                questionRound.getOnlineHearing().getOnlineHearingId() + " - Notification - Question round issued: " +
                questionRound.getQuestionRoundId(), String.class);
    }
}
