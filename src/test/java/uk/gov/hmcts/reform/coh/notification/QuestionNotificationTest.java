package uk.gov.hmcts.reform.coh.notification;

import org.junit.Before;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.Notification.QuestionNotification;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QuestionNotificationTest {

    private QuestionNotification questionNotification;

    private Question question;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setup(){
        restTemplate = mock(RestTemplate.class);
        when(restTemplate.postForEntity(anyString(),any(Question.class),any())).thenReturn(new ResponseEntity(HttpStatus.ACCEPTED));
        this.questionNotification = new QuestionNotification(restTemplate);

        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setUrl("http://someurl/notreal");
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setJurisdiction(jurisdiction);
        question = new Question();
        question.setOnlineHearing(onlineHearing);
    }
}
