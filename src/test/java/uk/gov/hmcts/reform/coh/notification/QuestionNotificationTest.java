package uk.gov.hmcts.reform.coh.notification;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.Notification.QuestionNotification;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

    @Test
    public void testQuestionNotificationReturnsTrueWith200Response(){
        boolean success = questionNotification.notifyQuestionState(question);
        assertTrue(success);
    }

    @Test
    public void testQuestionNotificationReturnsFalseWithAnyBadResponse(){
        RestTemplate restTemplate = mock(RestTemplate.class);
        ResponseEntity responseEntity = new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.postForEntity(anyString(), any(Question.class),any())).thenReturn(responseEntity);

        QuestionNotification questionNotification = new QuestionNotification(restTemplate);
        boolean success = questionNotification.notifyQuestionState(question);
        assertFalse(success);
    }

    @Test(expected = NullPointerException.class)
    public void testNotifyJurisdictionThrowsNullPointerExceptionWithNoUrl(){
        Jurisdiction jurisdiction = new Jurisdiction();
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setJurisdiction(jurisdiction);
        Question question = new Question();
        question.setOnlineHearing(onlineHearing);

        questionNotification.notifyQuestionState(question);
    }

    @Test
    public void testNotifyJurisdictionCallsRestTemplate(){
        questionNotification.notifyQuestionState(question);

        verify(restTemplate).postForEntity(anyString(), any(Question.class),any());
    }
}
